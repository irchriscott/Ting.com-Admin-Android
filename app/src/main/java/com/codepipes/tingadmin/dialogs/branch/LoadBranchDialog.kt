package com.codepipes.tingadmin.dialogs.branch

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.caverock.androidsvg.SVG
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.models.Branch
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_branch_load.*
import kotlinx.android.synthetic.main.dialog_branch_load.view.*

class LoadBranchDialog : DialogFragment(), OnMapReadyCallback {

    private lateinit var branch: Branch
    private lateinit var mMap: GoogleMap

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_branch_load, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        branch = Gson().fromJson(arguments?.getString(Constants.BRANCH_KEY), Branch::class.java)
        val mapFragment = fragmentManager!!.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        view.branch_name.text = branch.name
        view.branch_address.text = branch.address

        mapFragment.map.view!!.isClickable = false
        view.dialog_button_close.setOnClickListener { dialog?.dismiss() }

        return view
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val assetManager = context!!.assets
        val sharp = SVG.getFromAsset(assetManager, "restaurant_pin.svg")
        val mapPin = UtilsFunctions.vectorToBitmap(sharp, context!!)

        try {
            mMap.addMarker(MarkerOptions().position(LatLng(branch.latitude, branch.longitude)).title(branch.address).icon(mapPin))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(branch.latitude, branch.longitude), GOOGLE_MAPS_ZOOM))
        } catch (e: Exception){ TingToast(context!!, activity!!.resources.getString(R.string.error_internet), TingToastType.ERROR).showToast(
            Toast.LENGTH_LONG) }
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog?.window!!.setLayout(width, height)
        }
    }

    override fun onDetach() {
        super.onDetach()
        try {
            val f = fragmentManager!!.findFragmentById(R.id.map)
            fragmentManager!!.beginTransaction().remove(f!!).commit()
        } catch (e: Exception) {}
    }

    override fun dismiss() {
        super.dismiss()
        val f = fragmentManager!!.findFragmentById(R.id.map)
        fragmentManager!!.beginTransaction().remove(f!!).commit()
    }

    companion object {
        private const val GOOGLE_MAPS_ZOOM = 16.0f
    }
}