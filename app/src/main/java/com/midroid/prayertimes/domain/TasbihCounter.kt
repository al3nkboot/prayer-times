package com.midroid.prayertimes.domain

enum class TapEvent { NONE, ROUND_COMPLETE }
class TasbihCounter(var target:Int=33,var count:Int=0) {
 val rounds:Int get()=count/target
 fun tap():TapEvent { count++;return if(count%target==0)TapEvent.ROUND_COMPLETE else TapEvent.NONE }
 fun reset(){count=0}
}
