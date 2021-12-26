package com.bowman.dimension


import android.app.Activity
import com.tencent.cos.xml.CosXmlService
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.transfer.TransferConfig
import com.tencent.cos.xml.transfer.TransferManager
import com.tencent.qcloud.core.auth.QCloudCredentialProvider
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider


object CloudFileConstants {
    // keyDuration 为请求中的密钥有效期，单位为秒
    private val myCredentialProvider: QCloudCredentialProvider = ShortTimeCredentialProvider(
        CloudKey.secretId,
        CloudKey.secretKey,
        60 * 60
    )

    //    private final static QCloudCredentialProvider myCredentialProvider = new MySessionCredentialProvider();
    // 存储桶所在地域简称，例如广州地区是 ap-guangzhou
    private const val region = "ap-shanghai"

    // 创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
    private val serviceConfig: CosXmlServiceConfig = CosXmlServiceConfig.Builder()
        .setRegion(region)
        .isHttps(true) // 使用 HTTPS 请求, 默认为 HTTP 请求
        .builder()

    // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
    private val transferConfig: TransferConfig = TransferConfig.Builder().build()

    const val bucket = "dimension-1301874737" //存储桶，格式：BucketName-APPID

    fun getCosXmlService(activity: Activity): CosXmlService {
        return CosXmlService(
            activity,
            serviceConfig,
            myCredentialProvider
        )
    }

    fun getTransferManager(activity: Activity): TransferManager {
        return TransferManager(getCosXmlService(activity), transferConfig)
    }

    enum class TransferType {
        UPLOAD, DOWNLOAD
    }
}
