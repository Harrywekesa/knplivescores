package com.polyscores.kenya.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PresenceRepository {

    private val database = FirebaseDatabase.getInstance()
    private val presenceRef = database.getReference("presence")
    private val connectedRef = database.getReference(".info/connected")

    private val _activeDeviceCount = MutableStateFlow(0)
    val activeDeviceCount: StateFlow<Int> = _activeDeviceCount.asStateFlow()

    init {
        // Listen to total active devices
        presenceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (child in snapshot.children) {
                    val online = child.child("online").getValue(Boolean::class.java) ?: false
                    if (online) {
                        count++
                    }
                }
                _activeDeviceCount.value = count
            }

            override fun onCancelled(error: DatabaseError) {
                // Ignore
            }
        })
    }

    private var isTracking = false
    private var activeListener: ValueEventListener? = null
    private var currentDeviceId: String? = null

    fun startPresenceTracking(deviceId: String) {
        if (deviceId.isBlank()) return
        if (isTracking && currentDeviceId == deviceId) return // Already tracking this device

        // If we were tracking another device ID, clean up first
        stopPresenceTracking()

        currentDeviceId = deviceId
        isTracking = true
        val myConnectionsRef = presenceRef.child(deviceId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    // When this device disconnects, update status to false
                    myConnectionsRef.child("online").onDisconnect().setValue(false)
                    myConnectionsRef.child("lastDisconnect").onDisconnect().setValue(ServerValue.TIMESTAMP)

                    // Add this device to the presence list
                    myConnectionsRef.child("online").setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        activeListener = listener
        connectedRef.addValueEventListener(listener)
    }

    fun stopPresenceTracking() {
        activeListener?.let {
            connectedRef.removeEventListener(it)
        }
        currentDeviceId?.let { deviceId ->
            presenceRef.child(deviceId).child("online").setValue(false)
        }
        activeListener = null
        currentDeviceId = null
        isTracking = false
    }
}
