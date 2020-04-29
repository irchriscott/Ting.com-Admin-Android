package com.codepipes.tingadmin.fragments.sidebar


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.adapters.branch.BranchTableViewAdapter
import com.codepipes.tingadmin.dialogs.branch.AddBranchDialog
import com.codepipes.tingadmin.events.BranchesTableViewListener
import com.codepipes.tingadmin.interfaces.DataUpdatedListener
import com.codepipes.tingadmin.interfaces.FormDialogListener
import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Branch
import com.codepipes.tingadmin.models.ServerResponse
import com.codepipes.tingadmin.providers.TingClient
import com.codepipes.tingadmin.providers.UserAuthentication
import com.codepipes.tingadmin.utils.Routes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_branches.view.*
import kotlinx.android.synthetic.main.fragment_branches.view.empty_data
import kotlinx.android.synthetic.main.fragment_branches.view.progress_loader
import kotlinx.android.synthetic.main.include_empty_data.view.*
import java.lang.Exception


class BranchesFragment : Fragment() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var session: Administrator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_branches, container, false)

        userAuthentication = UserAuthentication(context!!)
        session = userAuthentication.get()!!

        if(!session.permissions.contains("can_add_branch")){
            view.button_add_new_branch.isClickable = false
            view.button_add_new_branch.visibility = View.GONE
        }

        view.button_add_new_branch.setOnClickListener {
            val addBranchDialog = AddBranchDialog()
            addBranchDialog.setFormDialogListener(object : FormDialogListener {
                override fun onSave() {
                    activity?.runOnUiThread {
                        addBranchDialog.dismiss()
                        loadBranches(view)
                    }
                }
                override fun onCancel() { addBranchDialog.dismiss() }
            })
            addBranchDialog.show(fragmentManager!!, addBranchDialog.tag)
        }

        loadBranches(view)

        return view
    }

    @SuppressLint("DefaultLocale")
    private fun loadBranches(view: View) {

        val gson = Gson()

        TingClient.getRequest(Routes.branchesAll, null, session.token) { _, isSuccess, result ->
            activity?.runOnUiThread {
                view.progress_loader.visibility = View.GONE
                if(isSuccess) {
                    try {
                        view.branches_table_view.visibility = View.VISIBLE
                        view.empty_data.visibility = View.GONE

                        val branches =
                            gson.fromJson<List<Branch>>(result, object : TypeToken<List<Branch>>(){}.type)

                        val branchTableViewAdapter = BranchTableViewAdapter(context!!)
                        view.branches_table_view.adapter = branchTableViewAdapter
                        branchTableViewAdapter.setBranchesList(branches)
                        view.branches_table_view.tableViewListener =
                            BranchesTableViewListener(
                                view.branches_table_view,
                                branches.toMutableList(),
                                context!!, fragmentManager!!,
                                object : DataUpdatedListener {
                                    override fun onDataUpdated() { activity?.runOnUiThread { loadBranches(view) } }
                                }, activity!! )

                    } catch (e: Exception) {
                        view.branches_table_view.visibility = View.GONE
                        view.empty_data.visibility = View.VISIBLE
                        view.empty_data.empty_image.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_exclamation_white))
                        try {
                            val serverResponse = gson.fromJson(result, ServerResponse::class.java)
                            view.empty_data.empty_text.text = serverResponse.message
                        } catch (e: Exception) { view.empty_data.empty_text.text = e.localizedMessage }
                    }
                } else {
                    view.branches_table_view.visibility = View.GONE
                    view.empty_data.visibility = View.VISIBLE
                    view.empty_data.empty_image.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_exclamation_white))
                    view.empty_data.empty_text.text = result.capitalize()
                }
            }
        }
    }
}
