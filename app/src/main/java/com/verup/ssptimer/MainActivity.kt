package com.verup.ssptimer

import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.verup.ssptimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var soundManager: SoundManager  //別ファイルのクラスのインスタンス化
    private var runnableDown : RunnableDown? = null  //mainActivity内
    private var runnableUp : RunnableUp? = null

    val handler = Handler()  //一度だけ代入

    private var timerStarted = false //最初は停止
    private var pomoTimerStarted = false //最初は停止
    private var pomoTimerStarted2 = false //最初は停止
    private var soundRunning = false //音が鳴っているかどうか

    var timeValue = 0       //何度も代入
    var pomodoroTimeTime = 0

    var h = 0
    var m = 0
    var s = 0
    var pomoH = 0
    var pomoM = 0
    var pomoS = 0

    //音鳴らす//////////////////////////////////////////////////////////////////////////////
    private lateinit var soundPool: SoundPool
    private var soundResId = 0
    private var soundOne = 0
    var streamId = 0
    var streamId2 = 0

    inner class RunnableUp(var timeRunUp: Int): Runnable{
        override fun run() {
            timeValue++
            timeToText(timeValue)?.let {   //TextViewを更新   ?.letを用いて、nullではない場合のみ更新
                binding.showTextTime.text = it    // timeToText(timeValue)の値がlet内ではitとして使える
            }

            pomodoroTimeTime = timeValue / 5
            handler.postDelayed(this, 1000)
            Log.d("debug", "runnable   " + timeValue)

            if (timeValue >= 36000) {
                resetTimer()
            }
        }

        fun start(): RunnableUp {
            Log.d("debug", "fun start")
            handler.post(this)
            return this
        }
        fun stop(): RunnableUp {
            Log.d("debug", "fun stop")
            handler.removeCallbacks(this)
            return this
        }
    }

    inner class RunnableDown(var timeRun: Int): Runnable{
        override fun run() {
            timeToText(pomodoroTimeTime)?.let{
                binding.showTextTime.text = it
            }
            if(pomodoroTimeTime <= 0){
                playOneReady()
                //soundPlay()
                Log.d("debug", "soundRunning 1    " + soundRunning)

            }else{
                pomodoroTimeTime--
                handler.postDelayed(this, 1000)
                Log.d("debug", "runnableDown   " + pomodoroTimeTime)
            }
        }

        fun breakStart(): RunnableDown {
            handler.post(this)
            return this;
        }

        fun breakStop(): RunnableDown {
            handler.removeCallbacks(this)
            return this
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        soundManager = SoundManager(this)

        Log.d("debug", "onCreate")

        binding.playStop.setOnClickListener{
            //カウントUP が動作⇒停止
            if (timerStarted && !pomoTimerStarted && !pomoTimerStarted2 && !soundRunning) {
                Log.d("debug", "stopTimer btn")
                stopTimer()
            }
            //カウントDown 停止⇒再生
            else if(!timerStarted && !pomoTimerStarted && !pomoTimerStarted2 && !soundRunning) {
                Log.d("debug", "startTimer btn")
                startTimer()
            }
            //カウントdown が動作⇒停止
            else if(!timerStarted && pomoTimerStarted && pomoTimerStarted2) {
                Log.d("debug", "breakStopTimer btn")
                breakStopTimer()
            }
            //カウントdown が停止⇒動作
            else if(!timerStarted && !pomoTimerStarted && pomoTimerStarted2) {
                Log.d("debug", "breakStartTimer btn")
                breakStartTimer()
            }

            else if(soundRunning) { //音が鳴っている
                Log.d("debug", "stopAlarm btn onCreate")
                //stopAlarm()
                soundManager.cancelOne()
                cancelOneLater()
            }

            else {
                Log.d("debug", "else btn")
                resetTimer()
            }
        }

        binding.reset.setOnClickListener {
            Log.d("debug", "reset Btn onCreate")

            //ここはちゃんと動いている
            if (soundRunning){ //音なっている
                //stopAlarm() //これやんないほうがいい
            }
            else {    //音なってない
                resetTimer()
            }
        }
    }

    private fun startTimer(){
        Log.d("debug", "startTimer")

        binding.playStop.setImageResource(R.drawable.stop)

        runnableUp = RunnableUp(timeValue).start()

        timerStarted = true
        pomoTimerStarted = false
        pomoTimerStarted2 = false
        soundRunning = false
    }

    private fun stopTimer(){
        Log.d("debug", "stopTimer")

        binding.playStop.setImageResource(R.drawable.play)

        runnableUp?.stop()

        timerStarted = false
        pomoTimerStarted = false
        pomoTimerStarted2 = false
        soundRunning = false

        timeDialogFragmentClickListener()   //fragment処理
    }

    private fun breakStartTimer(){
        Log.d("debug", "breakStartTimer")
        binding.playStop.setImageResource(R.drawable.stop)

        runnableDown = RunnableDown(pomodoroTimeTime).breakStart()

        timerStarted = false
        pomoTimerStarted = true
        pomoTimerStarted2 = true
        soundRunning = false
    }

    private fun breakStopTimer(){
        Log.d("debug", "breakStopTimer")

        binding.playStop.setImageResource(R.drawable.play)

        runnableDown?.breakStop()

        timerStarted = false
        pomoTimerStarted = false
        pomoTimerStarted2 = true
        soundRunning = false
    }


    public fun breakTimerReady(){
        Log.d("debug", "breakTimerReady")

        timeValue = 0

        timerStarted = false
        pomoTimerStarted = true
        pomoTimerStarted2 = true
        soundRunning = false

        //binding.timeText2.text = timeToText(pomodoroTimeTime)
        binding.showTextTime.text = timeToText(pomodoroTimeTime)  //時間を表示
        //pomodoroTimeTime += 1 //タイマーに +1s する
        binding.playStop.setImageResource(R.drawable.stop) //アラーム停止用にstopを表示

        breakTimer()
    }


    private fun breakTimer(){
        Log.d("debug", "breakTimer")
        runnableDown = RunnableDown(pomodoroTimeTime).breakStart()
    }

    public fun resetTimer(){
        Log.d("debug", "resetTimer")

        binding.playStop.setImageResource(R.drawable.play)

        runnableUp?.stop()


        timeValue = 0
        pomodoroTimeTime = 0

        timerStarted = false

        // timeToTextの引数はデフォルト値が設定されているので、引数省略できる
        timeToText()?.let {
            binding.showTextTime.text = it
        }
    }

    fun playOneReady() {
        Log.d("debug", "playOneReady")
        binding.playStop.setImageResource(R.drawable.stop)

        timerStarted = false
        pomoTimerStarted = false
        pomoTimerStarted2 = false
        soundRunning = true

        soundManager.playOne()
    }

     fun cancelOneLater() {
        Log.d("debug", "cancelOneLater")
         binding.playStop.setImageResource(R.drawable.play)

        timeValue = 0
        pomodoroTimeTime = 0

        timerStarted = false
        pomoTimerStarted = false
        pomoTimerStarted2 = false
        soundRunning = false
    }


    // 数値を00:00:00形式の文字列に変換する関数
    // 引数timeにはデフォルト値0を設定、返却する型はnullableなString?型
    private fun timeToText(time: Int = 0): String? {
        // if式は値を返すため、そのままreturnできる
        return if (time < 0) {
            null
        } else if (time == 0) {
            "0:00:00"
        } else {
            h = time / 3600
            m = time % 3600 / 60
            s = time % 60
            "%1$01d:%2$02d:%3$02d".format(h,m,s)
        }
    }

    public fun pomodoro(time: Int) {
        val pomoTime = time / 5
        pomoH = pomoTime / 3600
        pomoM = pomoTime % 3600 / 60
        pomoS = pomoTime % 60
    }

    //fragment
    private fun timeDialogFragmentClickListener() {

        pomodoroTimeTime = timeValue / 5

        val timeDialogFragment = TimeDialogFragment()
        pomodoro(timeValue) //重要

        //DialogFragmentに文字セット
        val args = Bundle()
        //数字で送る Int
        args.putInt("hourInt", h)
        args.putInt("minuteInt", m)
        args.putInt("secondInt", s)
        args.putInt("pomoHourInt", pomoH)
        args.putInt("pomoMinuteInt", pomoM)
        args.putInt("pomoSecondInt", pomoS)
        timeDialogFragment.setArguments(args)
        timeDialogFragment.show(supportFragmentManager, "aaa")
    }

    //アクティビティが表示されたとき
    override fun onResume() {
        super.onResume()
        Log.d("debug", "onResume")
    }

    override fun onPause() { //アクティビティ非表示   (バックグラウンドでの動作）
        super.onPause()
        Log.d("debug", "onPause")

        if(timeValue >= 36000){
            resetTimer()
        }
    }
}
