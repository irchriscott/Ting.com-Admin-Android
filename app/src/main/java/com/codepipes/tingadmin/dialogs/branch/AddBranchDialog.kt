package com.codepipes.tingadmin.dialogs.branch

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.caverock.androidsvg.SVG
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.dialogs.messages.ProgressOverlay
import com.codepipes.tingadmin.dialogs.messages.SelectDialog
import com.codepipes.tingadmin.dialogs.messages.TingToast
import com.codepipes.tingadmin.dialogs.messages.TingToastType
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.interfaces.SelectItemListener
import com.codepipes.tingadmin.models.Branch
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Constants
import com.codepipes.tingadmin.utils.Routes
import com.codepipes.tingadmin.utils.UtilsFunctions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.PlaceFilter
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_branch_edit.*
import kotlinx.android.synthetic.main.dialog_branch_edit.view.*
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddBranchDialog : DialogFragment(), OnMapReadyCallback {

    private lateinit var layoutView: View

    private lateinit var mMap: GoogleMap

    private lateinit var geocoder: Geocoder
    private var formDialogListener: FormDialogListener? = null

    private var branchLatitude: Double = 0.0
    private var branchLongitude: Double = 0.0
    private var branchAddress: String = ""
    private var branchCountry: String = ""
    private var branchTown: String = ""
    private var branchRegion: String = ""
    private var branchRoad: String = ""
    private var branchPlaceId: String = ""

    private var selectedBranchType: Int = 0

    private lateinit var mapPin: BitmapDescriptor
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        layoutView = inflater.inflate(R.layout.dialog_branch_edit, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val session = UserAuthentication(context!!).get()

        layoutView.dialog_title.text = "Edit Branch"

        val mapFragment = fragmentManager!!.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapFragment.map.view!!.isClickable = true

        geocoder = Geocoder(activity, Locale.getDefault())
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)

        val assetManager = context!!.assets
        val sharp = SVG.getFromAsset(assetManager, "restaurant_pin.svg")
        mapPin = UtilsFunctions.vectorToBitmap(sharp, context!!)

        layoutView.branch_type_select.setOnClickListener {
            val selectDialog = SelectDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Branch Type")
            selectDialog.arguments = bundle
            selectDialog.setItems(
                Constants.RESTAURANT_TYPES.sortedBy { it.id }.map { it.name },
                object :
                    SelectItemListener {
                    override fun onSelectItem(position: Int) {
                        selectedBranchType = position + 1
                        layoutView.selected_branch_type.text =
                            Constants.RESTAURANT_TYPES.first { it.id == selectedBranchType }.name
                        layoutView.selected_branch_type.setTextColor(resources.getColor(R.color.colorGray))
                        selectDialog.dismiss()
                    }
                })
            selectDialog.show(fragmentManager!!, selectDialog.tag)
        }

        layoutView.dialog_button_cancel.setOnClickListener {
            if (formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        layoutView.dialog_button_save.setOnClickListener {

            if (selectedBranchType != 0 && branchLongitude != 0.0 && branchLatitude != 0.0 && branchAddress != "") {
                val data = HashMap<String, String>()

                data["name"] = layoutView.branch_name.text.toString()
                data["address"] = branchAddress
                data["longitude"] = branchLongitude.toString()
                data["latitude"] = branchLatitude.toString()
                data["place_id"] = branchPlaceId
                data["region"] = branchRegion
                data["road"] = branchRoad
                data["email"] = layoutView.branch_email.text.toString()
                data["phone"] = layoutView.branch_phone_number.text.toString()
                data["country"] = branchCountry
                data["town"] = branchTown

                val progressOverlay = ProgressOverlay()
                progressOverlay.show(fragmentManager!!, progressOverlay.tag)

                TingClient.postRequest(Routes.addNewBranch, data, null, session?.token) { _, isSuccess, result ->
                    activity?.runOnUiThread {
                        progressOverlay.dismiss()
                        if (isSuccess) {
                            try {
                                val serverResponse = Gson().fromJson(result, ServerResponse::class.java)
                                TingToast(context!!,
                                    serverResponse.message,
                                    if (serverResponse.type == "success") { TingToastType.SUCCESS } else { TingToastType.ERROR }
                                ).showToast(Toast.LENGTH_LONG)
                                if (serverResponse.type == "success") {
                                    if (formDialogListener != null) {
                                        formDialogListener?.onSave()
                                    } else {
                                        TingToast(context!!, "State Changed. Please, Try Again", TingToastType.DEFAULT).showToast(Toast.LENGTH_LONG)
                                        dialog?.dismiss()
                                    }
                                }
                            } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                        } else { TingToast(context!!, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                    }
                }
            } else { TingToast(context!!, "Select All Required Fields", TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
        }

        return layoutView
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getCurrentLocation()

        mMap.setOnMapClickListener {
            mMap.clear()
            try {
                mMap.addMarker(MarkerOptions().position(it).title("").icon(mapPin))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, GOOGLE_MAPS_ZOOM))
                this.setLocationVariable(it)
            } catch (e: Exception) { TingToast(context!!, resources.getString(R.string.error_internet), TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if(it != null) {
                    val latLng = LatLng(it.latitude, it.longitude)
                    mMap.addMarker(MarkerOptions().position(latLng).title("").icon(mapPin))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, GOOGLE_MAPS_ZOOM))
                    setLocationVariable(latLng)
                } else { activity?.runOnUiThread { TingToast(context!!, "Location Not Found. Try Again", TingToastType.ERROR).showToast(Toast.LENGTH_LONG) } }
            }.addOnFailureListener {
                activity?.runOnUiThread { TingToast(context!!, it.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
            }
        } else {
            ActivityCompat.requestPermissions(activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    private fun setLocationVariable(latLng: LatLng) {
        activity?.runOnUiThread {
            try {
                val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)[0]
                branchLatitude = latLng.latitude
                branchLongitude = latLng.longitude
                branchAddress = address.getAddressLine(0)
                branchCountry = address.countryName
                branchTown = address.locality
                branchRegion = address.subLocality //address.adminArea
                branchRoad = address.thoroughfare
                branchPlaceId = UtilsFunctions.getToken(32)

                //address.subThoroughfare => Plot, Building Number

                layoutView.branch_address.setText(branchAddress)
                layoutView.branch_region.setText(branchRegion)
                layoutView.branch_road.setText(branchRoad)

            } catch (e: java.lang.Exception) { TingToast(context!!, resources.getString(R.string.error_internet), TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_LOCATION) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            }
        }
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
        } catch (e: java.lang.Exception) { }
    }

    override fun dismiss() {
        super.dismiss()
        val f = fragmentManager!!.findFragmentById(R.id.map)
        fragmentManager!!.beginTransaction().remove(f!!).commit()
    }

    public fun setFormDialogListener(listener: FormDialogListener) {
        this.formDialogListener = listener
    }

    companion object {
        private const val GOOGLE_MAPS_ZOOM = 16.0f
        private const val REQUEST_CODE_LOCATION = 12
    }
}