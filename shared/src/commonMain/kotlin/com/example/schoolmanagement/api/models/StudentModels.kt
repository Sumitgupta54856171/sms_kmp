package com.example.schoolmanagement.api.models

import kotlinx.serialization.Serializable

@Serializable
data class StudentListItem(
    val studentName: String? = null,
    val name: String? = null,
    val studentId: Int? = null,
    val scholarNo: String? = null,
    val scholar_no: String? = null,
    val faterhName: String? = null, // Backend uses faterhName
    val father_name: String? = null,
    val motherName: String? = null,
    val status: String? = null,
    val className: String? = null,
    val class_no: String? = null,
    val rollNo: String? = null,
    val roll_no: String? = null,
    val rolleNo: String? = null, // Backend inconsistency alias
    val id: Int? = null,
    val enrollmentId: Int? = null,
    val email: String? = null,
    val phone: String? = null,
    val gender: String? = null,
    val category: String? = null,
    val dob: String? = null,
    val sssmid: String? = null,
    val aadhaar: String? = null,
    val address: String? = null,
    val apaarId: String? = null,
    val penId: String? = null
)

@Serializable
data class BankDetailData(
    val bankDetailId: Int? = null,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val ifscCode: String? = null,
    val AccountHolderName: String? = null,
    val branchName: String? = null,
    val studentId: Int? = null
)

@Serializable
data class PhotoData(
    val id: Int? = null,
    val fileName: String? = null,
    val contentType: String? = null,
    val filePath: String? = null,
    val fileSize: Long? = null
)

@Serializable
data class StudentDetailResponse(
    val student: StudentListItem? = null,
    val bank: BankDetailData? = null,
    val photo: PhotoData? = null,
    val enrollment: List<StudentEnrollment>? = null
)

@Serializable
data class StudentEnrollment(
    val enrollmentId: Int? = null,
    val class_no: String? = null,
    val roll_no: String? = null
)

@Serializable
data class BulkRollNoPayload(
    val studentId: Int,
    val rollno: String
)
