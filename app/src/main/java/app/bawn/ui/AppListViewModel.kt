package app.bawn.ui

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.bawn.BawnApplication
import app.bawn.data.LockedAppEntity
import app.bawn.util.RestrictionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// 1. Define the 3 States
enum class PermissionState {
    Active,     // Green (Service Running)
    Inactive,   // Orange (Service Off, but Safe to enable)
    Restricted  // Red (Service Off, Sideloaded/Blocked)
}

data class AppUiModel(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val isLocked: Boolean
)

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as BawnApplication).database.lockedAppDao()
    private val _apps = MutableStateFlow<List<AppUiModel>>(emptyList())
    val apps = _apps.asStateFlow()

    // 2. Use the Enum for State
    private val _permissionState = MutableStateFlow(PermissionState.Inactive)
    val permissionState = _permissionState.asStateFlow()

    init {
        loadApps()
        checkStatus()
    }

    // 3. Logic to determine Red vs Orange vs Green
    fun checkStatus() {
        val context = getApplication<Application>()
        val isServiceOn = RestrictionHelper.isServiceEnabled(context)
        val isRestricted = RestrictionHelper.isLikelyRestricted(context)

        _permissionState.value = when {
            isServiceOn -> PermissionState.Active      // Green
            isRestricted -> PermissionState.Restricted // Red
            else -> PermissionState.Inactive           // Orange
        }
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