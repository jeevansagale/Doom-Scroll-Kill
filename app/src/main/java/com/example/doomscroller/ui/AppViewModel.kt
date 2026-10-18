package com.example.doomscroller.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.doomscroller.data.AppCatalog
import com.example.doomscroller.data.AppRule
import com.example.doomscroller.data.InstalledApp
import com.example.doomscroller.data.PreferencesManager
import com.example.doomscroller.platform.PermissionManager
import com.example.doomscroller.data.monitor.UsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.example.doomscroller.data.PenaltyInfo
import com.example.doomscroller.domain.EnforcementManager
import com.example.doomscroller.platform.UsageMonitoringService

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val appCatalog = AppCatalog(application)
    private val prefs = PreferencesManager(application)
    private val permissionManager = PermissionManager(application)
    private val usageRepository = UsageRepository(application)
    val enforcementManager = EnforcementManager.getInstance(application)

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps

    private val _rules = MutableStateFlow<List<AppRule>>(emptyList())
    val rules: StateFlow<List<AppRule>> = _rules
    
    private val _todayUsage = MutableStateFlow<Map<String, Long>>(emptyMap())
    val todayUsage: StateFlow<Map<String, Long>> = _todayUsage

    private val _hasUsageAccess = MutableStateFlow(false)
    val hasUsageAccess: StateFlow<Boolean> = _hasUsageAccess

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission

    private val _reminderIntervalMinutes = MutableStateFlow(prefs.getReminderIntervalMinutes())
    val reminderIntervalMinutes: StateFlow<Int> = _reminderIntervalMinutes

    private val _ignoredReminderLimit = MutableStateFlow(prefs.getIgnoredReminderLimit())
    val ignoredReminderLimit: StateFlow<Int> = _ignoredReminderLimit

    private val _reminderCapPer15Min = MutableStateFlow(prefs.getReminderCapPer15Min())
    val reminderCapPer15Min: StateFlow<Int> = _reminderCapPer15Min

    private val _destinationPackage = MutableStateFlow(prefs.getDestinationPackage())
    val destinationPackage: StateFlow<String?> = _destinationPackage

    val activePenalty: StateFlow<PenaltyInfo?> = enforcementManager.activePenalty

    init {
        checkPermissions()
        loadRules()
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appCatalog.getLaunchableApps()
            _installedApps.value = apps
        }
    }

    fun loadRules() {
        viewModelScope.launch(Dispatchers.IO) {
            _rules.value = prefs.getRules()
        }
    }

    fun saveRule(rule: AppRule) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.saveRule(rule)
            _rules.value = prefs.getRules()
            UsageMonitoringService.triggerImmediateCheck(getApplication())
            refreshUsage()
        }
    }

    fun removeRule(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.removeRule(packageName)
            enforcementManager.expirePenaltyImmediately(packageName)
            _rules.value = prefs.getRules()
            UsageMonitoringService.triggerImmediateCheck(getApplication())
            refreshUsage()
        }
    }

    fun checkPermissions() {
        val usageAccess = permissionManager.hasUsageAccess()
        _hasUsageAccess.value = usageAccess
        _hasNotificationPermission.value = permissionManager.hasNotificationPermission()
        _hasOverlayPermission.value = permissionManager.hasOverlayPermission()
        
        if (usageAccess) {
            UsageMonitoringService.start(getApplication())
            refreshUsage()
        }
    }

    fun setReminderIntervalMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 60)
        prefs.setReminderIntervalMinutes(clamped)
        _reminderIntervalMinutes.value = clamped
    }

    fun setIgnoredReminderLimit(limit: Int) {
        val clamped = limit.coerceIn(1, 10)
        prefs.setIgnoredReminderLimit(clamped)
        _ignoredReminderLimit.value = clamped
    }

    fun setReminderCapPer15Min(cap: Int) {
        val clamped = cap.coerceIn(1, 6)
        prefs.setReminderCapPer15Min(clamped)
        _reminderCapPer15Min.value = clamped
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.setOnboardingCompleted(completed)
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.isOnboardingCompleted()
    }

    fun setDestinationPackage(packageName: String?) {
        // Self-launch protection: prevent DoomScroller from being chosen as its own destination
        if (packageName == getApplication<Application>().packageName) {
            return
        }
        prefs.setDestinationPackage(packageName)
        _destinationPackage.value = packageName
    }

    fun isDestinationValid(packageName: String?): Boolean {
        return enforcementManager.isPackageLaunchable(packageName)
    }

    fun getAppName(packageName: String): String {
        return enforcementManager.getAppName(packageName)
    }
    
    fun refreshUsage() {
        if (_hasUsageAccess.value) {
            viewModelScope.launch(Dispatchers.IO) {
                val snapshot = usageRepository.getUsageSnapshot()
                _todayUsage.value = snapshot.usageMap
                enforcementManager.evaluateRules(
                    usageMap = snapshot.usageMap,
                    rules = _rules.value,
                    foregroundPackage = snapshot.foregroundPackage
                )
            }
        }
    }
}
