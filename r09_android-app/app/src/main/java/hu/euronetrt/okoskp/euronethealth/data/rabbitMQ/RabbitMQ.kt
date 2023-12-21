package hu.euronetrt.okoskp.euronethealth.data.rabbitMQ

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

class RabbitMQ {

    private var mContext: Context
    private lateinit var rabbitMQFactory: ConnectionFactory
    private lateinit var rabbitMQConnection: Connection
    private lateinit var rabbitMQChannel: Channel

    constructor(mContext: Context) {
        this.mContext = mContext
    }

    companion object {
        private val TAG = "RABBITMQ"
        private val QUEUENAME = "mdani"
        private val host = "amqp://192.168.0.207"
        private val vHost = "/"
        private val portNumber = 5672
        private val username = "mdani"
        private val pass = "pass"

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: RabbitMQ? = null

        @Synchronized
        fun getInstance(mContext: Context): RabbitMQ {
            if (INSTANCE == null) {
                INSTANCE = RabbitMQ(mContext)
            }
            return INSTANCE!!
        }
    }

    fun connection() : Boolean {
        rabbitMQFactory = ConnectionFactory()
        rabbitMQFactory.setUri(host)
        rabbitMQFactory.username = username
        rabbitMQFactory.password = pass
        rabbitMQFactory.virtualHost = vHost
        rabbitMQFactory.port = portNumber
        try {
            rabbitMQConnection = rabbitMQFactory.newConnection()
        } catch (e: Exception) {
            Log.d(TAG, "error : ${e.message}")
            return false
        }
        return true
    }

    fun createChannel() : Boolean{

     //   Log.d(TAG, "createChannel!")
        try {
            rabbitMQChannel = rabbitMQConnection.createChannel()
            if(rabbitMQChannel.queueDeclarePassive(QUEUENAME).queue  == null){
                return false
            }
        }catch (e: java.lang.Exception){
       //     Log.d(TAG, "error : ${ e.message}")
            return false
        }
        return true
    }

    fun queue(queue: String) {
        //Log.d(TAG, "queue!")
        rabbitMQChannel.basicPublish("", QUEUENAME,null,queue.toByteArray())
    }

    fun closeChannel() {
   //     Log.d(TAG, "closeChannel!")
        rabbitMQChannel.close()
    }

    fun disConnection() {
     //   Log.d(TAG, "disConnection!")
        rabbitMQConnection.close()
    }
}