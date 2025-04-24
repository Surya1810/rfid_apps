package com.partnership.bjbdocumenttrackerreader.repository

import com.partnership.bjbdocumenttrackerreader.data.ResultWrapper
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.GetSearchDocument
import com.partnership.bjbdocumenttrackerreader.data.model.ItemStatus
import com.partnership.bjbdocumenttrackerreader.data.network.ApiService
import com.partnership.bjbdocumenttrackerreader.data.model.BaseResponse
import com.partnership.bjbdocumenttrackerreader.data.model.DetailAgunan
import com.partnership.bjbdocumenttrackerreader.data.model.DocumentDetail
import com.partnership.rfid.data.model.UploadData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    SearchRepository {

    }
