package com.example.myapplication.module

import android.content.Context
import com.example.myapplication.utils.ConstUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.metamask.androidsdk.*

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {
    @Provides
    fun provideDappMetadata(@ApplicationContext context: Context): DappMetadata {
        val dappMetadata = DappMetadata(
            "Mediva",
            "https://192.168.202.40${context.applicationInfo.name}.com"
        )
        return dappMetadata
    }

    @Provides
    fun provideEthereumFlow(@ApplicationContext context: Context, dappMetadata: DappMetadata): EthereumFlow {
        val infuraApiKey = ConstUtil.ethInfuraApiKey
        val readonlyRPCMap = mapOf(ConstUtil.ethNetworkId to ConstUtil.ethRpcUrl)
        val sdkOptions = SDKOptions(
            infuraAPIKey = infuraApiKey,
            readonlyRPCMap = readonlyRPCMap,
        )

        val ethereum = Ethereum(context, dappMetadata, sdkOptions)
        return EthereumFlow(ethereum)
    }
}