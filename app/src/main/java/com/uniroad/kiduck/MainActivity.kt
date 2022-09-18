package com.uniroad.kiduck

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uniroad.kiduck.databinding.ActivityMainBinding
import org.jetbrains.anko.startActivity


class PairingListAdapter(private val dataSet: ArrayList<BluetoothDevice>) :
    RecyclerView.Adapter<PairingListAdapter.ViewHolder>() {

    class ViewHolder(val gridLayout: GridLayout) : RecyclerView.ViewHolder(gridLayout)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val gridLayout = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.kiduck_pairing_item, viewGroup, false) as GridLayout

        return ViewHolder(gridLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val kiduckName: TextView = viewHolder.gridLayout.findViewById(R.id.KiDuckName)
        kiduckName.text = dataSet[position].name
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}



class MainActivity : AppCompatActivity() {
    private val PERMISSION_RESULT_CODE = 1334
    private lateinit var binding: ActivityMainBinding // activitiy_main.xml의 layout 요소에 접근을 위한 변수
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var pairingList: ArrayList<BluetoothDevice>
    private lateinit var pairingListAdapter: PairingListAdapter

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private var mStartForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val resultCode = result.resultCode
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // activitiy_main.xml의 layout에 연결
        setContentView(binding.root) // layout 보이기

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH) }?.also {
            Toast.makeText(this, "해당 기기가 Bluetooth를 지원하지 않아 어플을 사용하실 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "해당 기기가 BLE를 지원하지 않아 어플을 사용하실 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            val registerForResult = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                }
            }
            registerForResult.launch(enableBtIntent)
        }

        checkPermission()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.BLUETOOTH,
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_ADVERTISE,
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ),
//                1
//            )
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.BLUETOOTH,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ),
//                1
//            )
//        }

        pairingList = ArrayList<BluetoothDevice>(bluetoothAdapter?.bondedDevices)
        viewManager = GridLayoutManager(this, 2)
        pairingListAdapter = PairingListAdapter(pairingList)
        val pairingListView = findViewById<RecyclerView>(R.id.KiDuckPairingList).apply {
            layoutManager = viewManager
            adapter = pairingListAdapter
        }


        binding.addDevice.setOnClickListener {
            startActivity<ConnectKiduckActivity>(
                "bluetoothAdapter" to bluetoothAdapter
            )
        }

        binding.Kiduck1.setOnClickListener {
            startActivity<SummaryActivity>(
                "address" to ""
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSION_RESULT_CODE -> {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun checkPermission() {
        var permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        var arrayPermission = ArrayList<String>()
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            arrayPermission.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (arrayPermission.size > 0) {
            ActivityCompat.requestPermissions(this, arrayPermission.toTypedArray(), PERMISSION_RESULT_CODE)
        }
    }
}