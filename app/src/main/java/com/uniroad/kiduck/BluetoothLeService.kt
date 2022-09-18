package com.uniroad.kiduck

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*

class BluetoothLeService: Service() {
    val STATE_DISCONNECTED = 0
    val STATE_CONNECTING = 1
    val STATE_CONNECTED = 2

    var connectionState = STATE_DISCONNECTED
    var bluetoothGatt: BluetoothGatt? = null
    var deviceAddress: String = ""
    val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    inner class LocalBinder: Binder() {
        val service = this@BluetoothLeService
    }

    val binder = LocalBinder()
    override fun onBind(p0: Intent?): IBinder? = binder

    companion object {
        val ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "EXTRA_DATA"
        val UUID_DATA_NOTIFY = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
        val UUID_DATA_WRITE = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
        val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    fun connect(address: String): Boolean {
        bluetoothGatt?.let {
            if (address.equals(deviceAddress)) {
                if (it.connect()) {
                    connectionState = STATE_CONNECTING
                    return true
                } else return false
            }
        }

        val device = bluetoothAdapter.getRemoteDevice(address)
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
        deviceAddress = address
        connectionState = STATE_CONNECTING
        return true
    }

    val gattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            var intentAction = ""
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(ACTION_GATT_CONNECTED)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                else -> {}
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            byteArray: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, byteArray, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                else -> {}
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            byteArray: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, byteArray)
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)

        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic?) {
        val intent = Intent(action)

        characteristic?.let {
            when (it.uuid) {
                UUID_DATA_NOTIFY -> {
                    val data: String = it.getStringValue(0)
                    intent.putExtra(EXTRA_DATA, data)
                }
                else -> Log.d("broadcastUpdate", String.format("%s", it.value.toString()))
            }
        }

        sendBroadcast(intent)
    }

    @RequiresApi(33)
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data: String) {
        bluetoothGatt?.writeCharacteristic(characteristic, data.toByteArray(), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) //임의로 고침
    }

    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, enable)
        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG).apply {
            setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        }
        bluetoothGatt?.writeDescriptor(descriptor)
    }

    fun getSupportedGattServices(): List<BluetoothGattService>? {
        if (bluetoothGatt == null) return null
        return bluetoothGatt!!.services
    }
}