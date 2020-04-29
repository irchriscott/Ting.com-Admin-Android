package com.codepipes.tingadmin.adapters.branch

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_branch_edit.*
import kotlinx.android.synthetic.main.dialog_branch_edit.view.*
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EditBranchDialog : DialogFragment(), OnMapReadyCallback {

    private lateinit var layoutView: View

    private lateinit var branch: Branch
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

        layoutView.dialog_title.text = "Add Branch"

        branch = Gson().fromJson(arguments?.getString(Constants.BRANCH_KEY), Branch::class.java)
        val mapFragment = fragmentManager!!.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapFragment.map.view!!.isClickable = true

        branchLatitude = branch.latitude
        branchLongitude = branch.longitude
        branchAddress = branch.address
        branchCountry = branch.country
        branchTown = branch.town
        branchRegion = branch.region
        branchRoad = branch.road
        branchPlaceId = branch.placeId

        selectedBranchType = branch.type

        geocoder = Geocoder(activity, Locale.getDefault())

        layoutView.branch_address.setText(branchAddress)
        layoutView.branch_name.setText(branch.name)
        layoutView.branch_region.setText(branchRegion)
        layoutView.branch_road.setText(branchRoad)
        layoutView.selected_branch_type.text = Constants.RESTAURANT_TYPES.first { it.id == branch.type }.name
        layoutView.selected_branch_type.setTextColor(resources.getColor(R.color.colorGray))
        layoutView.branch_email.setText(branch.email)
        layoutView.branch_phone_number.setText(branch.phone)

        layoutView.branch_type_select.setOnClickListener {
            val selectDialog = SelectDialog()
            val bundle = Bundle()
            bundle.putString(Constants.CONFIRM_TITLE_KEY, "Select Branch Type")
            selectDialog.arguments = bundle
            selectDialog.setItems(Constants.RESTAURANT_TYPES.sortedBy { it.id }.map { it.name }, object :
                SelectItemListener {
                override fun onSelectItem(position: Int) {
                    selectedBranchType = position + 1
                    layoutView.selected_branch_type.text = Constants.RESTAURANT_TYPES.first { it.id == selectedBranchType }.name
                    layoutView.selected_branch_type.setTextColor(resources.getColor(R.color.colorGray))
                    selectDialog.dismiss()
                }
            })
            selectDialog.show(fragmentManager!!, selectDialog.tag)
        }

        layoutView.dialog_button_cancel.setOnClickListener {
            if(formDialogListener != null) {
                formDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        layoutView.dialog_button_save.setOnClickListener {

            val data = HashMap<String, String>()

            data["name"] = layoutView.branch_name.text.toString()
            data["address"] = branchAddress
            data["longitude"] = branchLongitude.toString()
            data["latitude"] =  branchLatitude.toString()
            data["place_id"] = branchPlaceId
            data["region"] = branchRegion
            data["road"] = branchRoad
            data["email"] = layoutView.branch_email.text.toString()
            data["phone"] = layoutView.branch_phone_number.text.toString()
            data["country"] = branchCountry
            data["town"] = branchTown

            val progressOverlay = ProgressOverlay()
            progressOverlay.show(fragmentManager!!, progressOverlay.tag)

            TingClient.postRequest("${Routes.updateBranch}${branch.id}/", data, null, session?.token) { _, isSuccess, result ->
                activity?.runOnUiThread {
                    progressOverlay.dismiss()
                    if(isSuccess) {
                        try {
                            val serverResponse = Gson().fromJson(result, ServerResponse::class.java)
                            TingToast(context!!, serverResponse.message, if(serverResponse.type == "success") { TingToastType.SUCCESS } else { TingToastType.ERROR }).showToast(Toast.LENGTH_LONG)
                            if(serverResponse.type == "success") {
                                if(formDialogListener != null) { formDialogListener?.onSave()
                                } else {
                                    TingToast(context!!, "State Changed. Please, Try Again", TingToastType.DEFAULT).showToast(
                                        Toast.LENGTH_LONG)
                                    dialog?.dismiss()
                                }
                            }
                        } catch (e: Exception) { TingToast(context!!, e.localizedMessage, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                    } else { TingToast(context!!, result, TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
                }
            }
        }

        return layoutView
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

        mMap.setOnMapClickListener {
            mMap.clear()
            try {
                mMap.addMarker(MarkerOptions().position(it).title(branch.name).icon(mapPin))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, GOOGLE_MAPS_ZOOM))
                this.setLocationVariable(it)
            } catch(e: Exception){ TingToast(context!!, resources.getString(R.string.error_internet), TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
        }
    }

    private fun setLocationVariable(latLng: LatLng) {
        try {
            val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)[0]
            branchLatitude = latLng.latitude
            branchLongitude = latLng.longitude
            branchAddress = address.getAddressLine(0)
            branchCountry = address.countryName
            branchTown = address.locality
            branchRegion = address.subLocality //address.adminArea
            branchRoad = address.thoroughfare
            branchPlaceId = branch.placeId

            //address.subThoroughfare => Plot, Building Number

            layoutView.branch_address.setText(branchAddress)
            layoutView.branch_region.setText(branchRegion)
            layoutView.branch_road.setText(branchRoad)

        } catch (e: java.lang.Exception) { TingToast(context!!, resources.getString(R.string.error_internet), TingToastType.ERROR).showToast(Toast.LENGTH_LONG) }
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
        } catch (e: java.lang.Exception) {}
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
    }
}