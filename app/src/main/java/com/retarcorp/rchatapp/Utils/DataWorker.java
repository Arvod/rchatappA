package com.retarcorp.rchatapp.Utils;

import com.retarcorp.rchatapp.Global;
import com.retarcorp.rchatapp.R;

import java.util.Date;

public class DataWorker {

    public static String getDifferenceBtwTime(Date dateTime) {
        long timeDifferenceMilliseconds = new Date().getTime() - dateTime.getTime();
        long diffSeconds = timeDifferenceMilliseconds / 1000;
        long diffMinutes = timeDifferenceMilliseconds / (60 * 1000);
        long diffHours = timeDifferenceMilliseconds / (60 * 60 * 1000);
        long diffDays = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24);
        long diffWeeks = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 7);
        long diffMonths = (long) (timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 30.41666666));
        long diffYears = timeDifferenceMilliseconds / (1000 * 60 * 60 * 24 * 365);

        if (diffSeconds < 1) {
            return Global.Ctx.getResources().getString(R.string.one_sec_ago);
        } else if (diffMinutes < 1) {
            return diffSeconds + " " + Global.Ctx.getResources().getString(R.string.seconds_ago);
        } else if (diffHours < 1) {
            return diffMinutes + " " + Global.Ctx.getResources().getString(R.string.minutes_ago);
        } else if (diffDays < 1) {
            return diffHours + " " + Global.Ctx.getResources().getString(R.string.hours_ago);
        } else if (diffWeeks < 1) {
            return diffDays + " " + Global.Ctx.getResources().getString(R.string.hours_ago);
        } else if (diffMonths < 1) {
            return diffWeeks + " " + Global.Ctx.getResources().getString(R.string.weeks_ago);
        } else if (diffYears < 12) {
            return diffMonths + " " + Global.Ctx.getResources().getString(R.string.months_ago);
        } else {
            return diffYears + " " + Global.Ctx.getResources().getString(R.string.years_ago);
        }
    }
}
