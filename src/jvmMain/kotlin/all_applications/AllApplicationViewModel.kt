package all_applications

import DataState
import FirebaseConstants.APPLICATIONS
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllApplicationViewModel{
    private val _stateApplications = mutableStateOf(DataState<List<ApplicationPojo>>())
    val stateApplications: State<DataState<List<ApplicationPojo>>> = _stateApplications


    var allApplications = mutableStateListOf<ApplicationPojo>()

    fun getApplications() {
        _stateApplications.value = DataState(isLoading = true)
        FirebaseDatabase.getInstance().reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataList: MutableList<ApplicationPojo> = ArrayList()

                for (dataSnap in snapshot.child(APPLICATIONS).children) {
                    dataSnap.getValue(ApplicationPojo::class.java)?.let {
                        dataList.add(it)
                    }
                }
                allApplications.clear()
                allApplications.addAll(dataList)
                _stateApplications.value = DataState(data = dataList)
            }

            override fun onCancelled(error: DatabaseError) {
                _stateApplications.value = DataState(error = error.message)
            }

        })
    }

    fun getApplications(type: ApplicationsType, keyWord: String = "") {
        if (allApplications.isNotEmpty())
        {
            val data = when (type) {
                ApplicationsType.All -> {
                    allApplications
                }
                ApplicationsType.Active -> {
                    allApplications.filter {
                        it.isApproved == true
                    }
                }
                ApplicationsType.DisActive -> {
                    allApplications.filter {
                        it.isApproved != true
                    }
                }

                ApplicationsType.Attended -> {
                    allApplications.filter {
                        it.isAttend == true
                    }.sortedByDescending {
                        it.attendDate
                    }
                }
            }
            if (keyWord.isEmpty()|| keyWord.isBlank())
                _stateApplications.value = DataState(data = data)
            else
                _stateApplications.value = DataState(data = data.filter {
                    it.name?.contains(keyWord) == true
                            || it.title?.contains(keyWord) == true
                            || it.jobTitle?.contains(keyWord) == true
                            || it.employer?.contains(keyWord) == true
                })
        }
    }

    private val _stateUpdateData = mutableStateOf(DataState<Boolean>())
    val stateUpdateData: State<DataState<Boolean>> = _stateUpdateData


    fun resetData() {
        _stateApplications.value = DataState()
        _stateUpdateData.value = DataState()
    }

}