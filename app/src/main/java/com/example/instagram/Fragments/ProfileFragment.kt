package com.example.instagram.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.instagram.R
import com.example.instagram.AccountSettingsActivity
import com.example.instagram.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*



class ProfileFragment : Fragment() {
    private lateinit var profileId:String
    private lateinit var firebaseUser:FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser= FirebaseAuth.getInstance().currentUser!!
        val pref=context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        if (pref != null)
        {
            this.profileId= pref.getString("profileId","none").toString()
        }

        if (profileId == firebaseUser.uid)
        {
            view.edit_account_settings_btn.text="Edit Profile"
        }
        else if (profileId != firebaseUser.uid)
        {
            checkFollowAndFollowingButtonStatus()
        }

        //عند الضغط علي زر الedit_account_settings_btn ينقلنا اليAccountSettingsActivity
        view.edit_account_settings_btn.setOnClickListener {
            val getButtonText=view.edit_account_settings_btn.text.toString()
            when
            {
                getButtonText=="Edit Profile" ->  startActivity(Intent(context,
                    AccountSettingsActivity::class.java))

                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(itl.toString())
                                .child("Following").child(profileId)
                                .setValue(true)
                    }
                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(itl.toString())
                                .setValue(true)
                    }
                }
                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(itl.toString())
                                .child("Following").child(profileId)
                                .removeValue()
                    }
                    firebaseUser?.uid.let { itl ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(itl.toString())
                                .removeValue()
                    }
                }
            }

        }

        getFollowers()
        getFollowings()
        userInfo()
        return view
    }



    private fun checkFollowAndFollowingButtonStatus()
    {
        val followingRef= firebaseUser?.uid.let { itl ->
            FirebaseDatabase.getInstance().reference
                    .child("Follow").child(itl.toString())
                    .child("Following")
        }
        if (followingRef != null)
        {
            followingRef.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(profileId).exists())
                    {
                        view?.edit_account_settings_btn?.text="Following"
                    }
                    else
                    {
                        view?.edit_account_settings_btn?.text="Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }

    private fun getFollowers()
    {
        val followingRef= FirebaseDatabase.getInstance().reference
                    .child("Follow").child("profileId")
                    .child("Followers")

        followingRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    view?.total_followers?.text=p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowings()
    {
        val followingRef= FirebaseDatabase.getInstance().reference
                    .child("Follow").child(profileId)
                    .child("Following")

        followingRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    view?.total_following?.text=p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun userInfo()
    {
        val usersRef=FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        usersRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists())
                {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)
                    view?.profile_fragment_username?.text=user!!.getUsername()
                    view?.full_name_profile_frag?.text=user!!.getFullname()
                    view?.bio_profile_frag?.text=user!!.getBio()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref=context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref=context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref=context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }
}