package com.verup.ssptimer

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class TimeDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.let {

            //数字を受け取る
            val receivedHourInt = requireArguments().getInt("hourInt", 0)
            val receivedMinuteInt = requireArguments().getInt("minuteInt", 0)
            val receivedSecondInt = requireArguments().getInt("secondInt", 0)

            val receivedPomoHourInt = requireArguments().getInt("pomoHourInt", 0)
            val receivedPomoMinuteInt = requireArguments().getInt("pomoMinuteInt", 0)
            val receivedPomoSecondInt = requireArguments().getInt("pomoSecondInt", 0)

            var showTime = changeTimeString(receivedHourInt, receivedMinuteInt, receivedSecondInt)
            var pomoTime = changeTimeString(receivedPomoHourInt, receivedPomoMinuteInt, receivedPomoSecondInt)

            val builder = AlertDialog.Builder(activity) //ダイアログビルダーを生成
            builder.setTitle("Zone" + " : " + showTime)
            builder.setMessage("Break" + " : " + pomoTime)

            builder.setPositiveButton("Break", DialogButtonClickListener())
            //builder.setNegativeButton("Restart", DialogButtonClickListener())
            builder.setNeutralButton("Cancel", DialogButtonClickListener())
            builder.create() //リターン
        }
        return dialog ?:  throw IllegalStateException("アクティビティがnullです")
    }

    private inner class DialogButtonClickListener : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, which: Int) {
            var msg = ""
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> { //breaktime 開始
                    msg = "Break Time Started"
                    var mainActivity = activity as MainActivity
                    mainActivity.breakTimerReady()
                }

                /*
                DialogInterface.BUTTON_NEGATIVE -> {
                    msg = "Restart Again"
                }
                 */

                DialogInterface.BUTTON_NEUTRAL -> {
                    msg = "Canceled"
                }
            }
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    //時間をIntからStringにする
    private fun changeTimeString(hour: Int, min: Int, sec: Int): String = String.format(
        "%01d:%02d:%02d",
        hour,
        min,
        sec
    )

}