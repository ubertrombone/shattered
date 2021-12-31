package com.example.shattered.model

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.NullPointerException

class ShatteredRepository {

    private val _auth = Firebase.auth
    val auth: FirebaseAuth get() = _auth
    val database = Firebase.database
    val usernameReference = database.getReference("usernames")
    val currentLevelReference = database.getReference("currentLevels")
    private val allLevelsReference = database.reference

    fun fetchUsername(liveData: MutableLiveData<UsernameItem>) {
        usernameReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val usernameValue: UsernameItem =
                        snapshot.child(_auth.uid.toString()).getValue(UsernameItem::class.java)
                            ?: UsernameItem(null)
                    liveData.postValue(usernameValue)
                }
                override fun onCancelled(error: DatabaseError) = println("Fail")
            })
    }

    fun fetchAllUsernames(liveData: MutableLiveData<List<String>>) {
        usernameReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val usernameValues: List<String> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(UsernameItem::class.java)?.username.toString()
                    }
                    liveData.postValue(usernameValues)
                }
                override fun onCancelled(error: DatabaseError) = println("Fail")
            })
    }

    fun fetchCurrentLevel(liveData: MutableLiveData<CurrentLevelItem>) {
        currentLevelReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentLevelValue: CurrentLevelItem =
                        snapshot.child(_auth.uid.toString()).getValue(CurrentLevelItem::class.java)
                            ?: CurrentLevelItem(null)
                    liveData.postValue(currentLevelValue)
                }
                override fun onCancelled(error: DatabaseError) = println("Fail")
            })
    }

    fun fetchAllLevels(liveData: MutableLiveData<List<AllLevelsItem>>) {
        allLevelsReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allLevelValues = mutableListOf<AllLevelsItem>()
                    snapshot.children.forEach { child ->
                        if (child.key.toString() == "usernames" || child.key.toString() == "currentLevels")
                            return@forEach
                        try {
                            allLevelValues.add(child.child(_auth.uid.toString()).getValue(AllLevelsItem::class.java)!!)
                            allLevelValues.sortBy { it.level }
                        } catch (e: NullPointerException) { return@forEach }

                    }
                    liveData.postValue(allLevelValues)
                }
                override fun onCancelled(error: DatabaseError) = println("Fail")
            })
    }

    fun fetchOneLevel(liveData: MutableLiveData<AllLevelsItem>, reference: String) {
        val ref = getLevelReference(reference)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val level: AllLevelsItem = snapshot.child(_auth.uid.toString()).getValue(AllLevelsItem::class.java)
                    ?: AllLevelsItem()
                liveData.postValue(level)
            }
            override fun onCancelled(error: DatabaseError) = println("Fail")
        })
    }

    fun fetchAllPlayersOnLevel(liveData: MutableLiveData<List<AllLevelsItem>>, level: String) {
        val levelReference = database.getReference(level)
        levelReference
            .orderByChild("score")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allPlayersOnLevel: List<AllLevelsItem> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(AllLevelsItem::class.java)!!
                    }
                    liveData.postValue(allPlayersOnLevel.reversed())
                }
                override fun onCancelled(error: DatabaseError) = println("Fail")
            })
    }

    fun updateDatabase(value: Any, databaseReference: DatabaseReference) =
        databaseReference.child(_auth.uid.toString()).setValue(value)

    fun getLevelReference(reference: String) = database.getReference("Level $reference")
}