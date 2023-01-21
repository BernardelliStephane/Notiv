package fr.steph.showmemories.utils

import android.annotation.SuppressLint
import android.widget.DatePicker
import java.time.LocalDate

// Stops months/years from changing along with days/months values
class UpdateDatePicker(private var savedDay: Int, private var savedMonth: Int, private var savedYear: Int) {

    @SuppressLint("NewApi")
    operator fun invoke(seasonDatePicker: DatePicker, day: Int, month: Int, year: Int) {
        if(day != savedDay){
            var newDay = day
            if(month != savedMonth){
                val monthUp = month > savedMonth && !(month == 11 && savedMonth == 0) || (month == 0 && savedMonth == 11)
                val monthLength = LocalDate.of(year, savedMonth + 1, 1).lengthOfMonth()
                newDay = if(monthUp) 1 else monthLength
                seasonDatePicker.updateDate(savedYear, savedMonth, newDay)
            }
            savedDay = newDay
        }
        else if(month != savedMonth){
            if(year != savedYear) seasonDatePicker.updateDate(savedYear, month, day)
            savedMonth = month
        }
        else savedYear = year
    }
}
