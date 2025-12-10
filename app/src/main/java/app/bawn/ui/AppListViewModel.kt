package app.bawn.ui

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.bawn.BawnApplication
import app.bawn.data.LockedAppEntity
import app.bawn.util.RestrictionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PermissionState {
    Active, Inactive, Restricted
}

data class AppUiModel(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val isLocked: Boolean
)

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as BawnApplication).database
    private val dao = db.lockedAppDao()
    private val securityDao = db.userSecurityDao()

    private val _apps = MutableStateFlow<List<AppUiModel>>(emptyList())
    val apps = _apps.asStateFlow()

    private val _permissionState = MutableStateFlow(PermissionState.Inactive)
    val permissionState = _permissionState.asStateFlow()

    // Null = Loading/Unknown, False = No PIN, True = PIN Set
    private val _isPinSet = MutableStateFlow<Boolean?>(null)
    val isPinSet = _isPinSet.asStateFlow()

    init {
        loadApps()
        checkStatus()
        checkPinStatus()
    }

    fun checkStatus() {
        val context = getApplication<Application>()
        val isServiceOn = RestrictionHelper.isServiceEnabled(context)
        val isRestricted = RestrictionHelper.isLikelyRestricted(context)

        _permissionState.value = when {
            isServiceOn -> PermissionState.Active
            isRestricted -> PermissionState.Restricted
            else -> PermissionState.Inactive
        }
    }

    fun checkPinStatus() {
        // CRITICAL FIX: Reset to null immediately so UI doesn't act on stale "false" data
        _isPinSet.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val hasPin = securityDao.hasPin()
            _isPinSet.value = hasPin
        }
    }

    // Helper to manually reset state before navigation
    fun resetPinState() {
        _isPinSet.value = null
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val installed = pm.getInstalledPackages(PackageManager.GET_META_DATA)
                .filter {
                    val intent = pm.getLaunchIntentForPackage(it.packageName)
                    intent != null && it.packageName != getApplication<Application>().packageName
                }

            dao.getAllLockedApps().collect { lockedList ->
                val lockedSet = lockedList.map { it.packageName }.toSet()
                val uiList = installed.map { pack ->
                    AppUiModel(
                        name = pack.applicationInfo?.loadLabel(pm).toString(),
                        packageName = pack.packageName,
                        icon = pack.applicationInfo?.loadIcon(pm),
                        isLocked = lockedSet.contains(pack.packageName)
                    )
                }.sortedBy { it.name }
                _apps.value = uiList
            }
        }
    }

    fun toggleLock(app: AppUiModel) {
        viewModelScope.launch(Dispatchers.IO) {
            if (app.isLocked) dao.unlockApp(app.packageName)
            else dao.lockApp(LockedAppEntity(app.packageName))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BawnApplication)
                AppListViewModel(app)
            }
        }
    }
}