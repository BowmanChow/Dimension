@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.bowman.dimension

import android.app.Activity
import com.google.android.material.snackbar.Snackbar
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.model.`object`.DeleteObjectRequest
import com.tencent.cos.xml.model.bucket.GetBucketRequest
import com.tencent.cos.xml.model.bucket.GetBucketResult
import com.tencent.cos.xml.model.tag.ListBucket
import com.tencent.cos.xml.transfer.COSXMLTask
import java.io.File
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


object CloudFileTransfer {

    fun transfer(
        transferType: CloudFileConstants.TransferType,
        activity: Activity,
        fileName: String?,
        operate: () -> Unit
    ) {
        val file = File(activity.filesDir, fileName)
        val cosxmlTask: COSXMLTask = if (transferType === CloudFileConstants.TransferType.UPLOAD) {
            val uploadId: String? = null    //若存在初始化分块上传的 UploadId，则赋值对应的 uploadId 值用于续传；否则，赋值 null
            CloudFileConstants.getTransferManager(activity)
                .upload(CloudFileConstants.bucket, fileName, file.toString(), uploadId)
        } else {
            CloudFileConstants.getTransferManager(activity).download(
                activity,
                CloudFileConstants.bucket,
                fileName,
                activity.filesDir.toString(),
                fileName
            )
        }
        //设置上传进度回调
        cosxmlTask.setCosXmlProgressListener { _: Long, _: Long -> }
        //设置返回结果回调
        cosxmlTask.setCosXmlResultListener(object : CosXmlResultListener {
            override fun onSuccess(
                request: CosXmlRequest,
                result: CosXmlResult
            ) {
//                COSXMLUploadTask.COSXMLUploadTaskResult cOSXMLUploadTaskResult =
//                        (COSXMLUploadTask.COSXMLUploadTaskResult) result;
                Snackbar.make(
                    activity.window.decorView,
                    "$transferType successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
                operate()
            }

            override fun onFail(
                request: CosXmlRequest,
                clientException: CosXmlClientException,
                serviceException: CosXmlServiceException
            ) {
                clientException.printStackTrace()
                Snackbar.make(
                    activity.window.decorView,
                    "Fail to $transferType",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
        //设置任务状态回调, 可以查看任务过程
        cosxmlTask.setTransferStateListener {
            // todo notify transfer state
        }
    }

    fun batchDownload(activity: Activity, fileNames: Array<String?>, operate: () -> Unit) {
        val successfully = intArrayOf(0)
        val lock: Lock = ReentrantLock()
        for (fileName in fileNames) {
            File(activity.filesDir, fileName)
            var cosxmlTask: COSXMLTask
            cosxmlTask = CloudFileConstants.getTransferManager(activity).download(
                activity,
                CloudFileConstants.bucket,
                fileName,
                activity.filesDir.toString(),
                fileName
            )
            //设置上传进度回调
            cosxmlTask.setCosXmlProgressListener { _: Long, _: Long -> }
            //设置返回结果回调
            cosxmlTask.setCosXmlResultListener(object : CosXmlResultListener {
                override fun onSuccess(
                    request: CosXmlRequest,
                    result: CosXmlResult
                ) {
//                    COSXMLUploadTask.COSXMLUploadTaskResult cOSXMLUploadTaskResult =
//                            (COSXMLUploadTask.COSXMLUploadTaskResult) result;
                    lock.lock()
                    successfully[0]++
                    if (successfully[0] == fileNames.size) {
                        operate()
                    }
                    lock.unlock()
                }

                override fun onFail(
                    request: CosXmlRequest,
                    clientException: CosXmlClientException,
                    serviceException: CosXmlServiceException
                ) {
                    clientException.printStackTrace()
                }
            })
            //设置任务状态回调, 可以查看任务过程
            cosxmlTask.setTransferStateListener {
                // todo notify transfer state
            }
        }
    }

    fun getFileList(activity: Activity, prefix: String, operate: (List<ListBucket.Contents>) -> Unit) {
        val getBucketRequest = GetBucketRequest(CloudFileConstants.bucket)
        // 前缀匹配，用来规定返回的对象前缀地址
        getBucketRequest.prefix = prefix
        CloudFileConstants.getCosXmlService(activity)
            .getBucketAsync(getBucketRequest, object : CosXmlResultListener {
                override fun onSuccess(request: CosXmlRequest, result: CosXmlResult) {
                    val getBucketResult = result as GetBucketResult
                    if (getBucketResult.listBucket.isTruncated) {
                        // 表示数据被截断，需要拉取下一页数据
                    }
                    operate(getBucketResult.listBucket.contentsList)
                }

                override fun onFail(
                    cosXmlRequest: CosXmlRequest,
                    clientException: CosXmlClientException,
                    serviceException: CosXmlServiceException
                ) {
                    clientException.printStackTrace()
                }
            })
    }

    fun deleteFile(activity: Activity, fileName: String?) {
        val deleteObjectRequest =
            DeleteObjectRequest(CloudFileConstants.bucket, fileName)
        CloudFileConstants.getCosXmlService(activity).deleteObjectAsync(deleteObjectRequest,
            object : CosXmlResultListener {
                override fun onSuccess(
                    cosXmlRequest: CosXmlRequest,
                    result: CosXmlResult
                ) {
//                      DeleteObjectResult deleteObjectResult = (DeleteObjectResult) result;
                    Snackbar.make(
                            activity.window.decorView,
                            "Delete successfully",
                            Snackbar.LENGTH_SHORT
                    ).show()
                }

                override fun onFail(
                    cosXmlRequest: CosXmlRequest,
                    clientException: CosXmlClientException,
                    serviceException: CosXmlServiceException
                ) {
                    clientException.printStackTrace()
                    Snackbar.make(
                            activity.window.decorView,
                            "Delete failed",
                            Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
