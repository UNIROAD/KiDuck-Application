package com.uniroad.kiduck

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uniroad.kiduck.databinding.ActivityConnectKiduckBinding
import org.jetbrains.anko.startActivity

class DividerItemDecoration(
    context: Context,
    resId: Int,
    val paddingLeft: Int,
    val paddingRight: Int
) : RecyclerView.ItemDecoration() {
    private var mDivider: Drawable? = null
    init{
        mDivider = ContextCompat.getDrawable(context, resId)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft + paddingRight
        val right = parent.width - parent.paddingRight - paddingRight
        val childCount = parent.childCount

        for (i in 0 until childCount){
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + (mDivider?.intrinsicHeight ?: 0)

            mDivider?.let{
                it.setBounds(left, top, right, bottom)
                it.draw(c)
            }
        }
    }
}

class RecyclerViewAdapter(private val dataSet: ArrayList<BluetoothDevice>) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    var mListener : OnItemClickListener? = null

    interface OnItemClickListener{
        fun onClick(view: View, position: Int)
    }

    class ViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.ble_scan_item, parent, false) as LinearLayout

        return ViewHolder(linearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val itemName: TextView = viewHolder.linearLayout.findViewById(R.id.item_name)
        val itemAddress: TextView = viewHolder.linearLayout.findViewById(R.id.item_address)

        itemName.text = dataSet[position].name
        itemAddress.text = dataSet[position].address

        if(mListener!=null){
            viewHolder?.itemView?.setOnClickListener{v->
                mListener?.onClick(v, position)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}

class ConnectKiduckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConnectKiduckBinding
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewAdapter : RecyclerViewAdapter

    private var bluetoothAdapter: BluetoothAdapter = intent.getSerializableExtra("bluetoothAdapter") as BluetoothAdapter
    private var mScanning: Boolean = false
    private var arrayDevice = ArrayList<BluetoothDevice>()
    private val SCAN_PERIOD = 1000
    private val handler = Handler(Looper.getMainLooper())

    private var bleGatt: BluetoothGatt? = null
    private var mContext:Context? = null

    private val scanCallback = object: ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scanCallback", "BLE Scan Failed : " + errorCode)
        }
        override fun onBatchScanResults(results: MutableList<ScanResult> ?) {
            super.onBatchScanResults(results)
            results?.let {
                // results is not null
                for(result in it) {
                    if(!arrayDevice.contains(result.device) && result.device.name!=null) arrayDevice.add(result.device)
                }
            }
        }
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                // result is not null
                if(!arrayDevice.contains(it.device) && it.device.name!=null) arrayDevice.add(it.device)
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun scanLeDevice(state:Boolean) = if(state) {
        handler.postDelayed({
            mScanning = false
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        }, SCAN_PERIOD)
        mScanning = true
        arrayDevice.clear()
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }
    else {
        mScanning = false
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectKiduckBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.getAdapter()
        viewManager = LinearLayoutManager(this)
        recyclerViewAdapter = RecyclerViewAdapter(arrayDevice)
        val recyclerView = findViewById<RecyclerView > (R.id.recyclerView).apply {
            layoutManager = viewManager
            adapter = recyclerViewAdapter
            addItemDecoration(DividerItemDecoration(context, R.drawable.line_divider,15,15))
        }
    }

    override fun onResume(){
        super.onResume()
        scanLeDevice(true)
        mContext = this
        recyclerViewAdapter.mListener = object : RecyclerViewAdapter.OnItemClickListener{
            override fun onClick(view: View, position: Int) {
                scanLeDevice(false) // scan 중지
                val device = arrayDevice.get(position)

                startActivity<SummaryActivity>(
                    "address" to device.address
                )
            }
        }
    }


}

private fun Handler.postDelayed(function: () -> Unit?, scanPeriod: Int) {

}

