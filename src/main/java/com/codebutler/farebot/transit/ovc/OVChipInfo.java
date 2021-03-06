/*
 * OVChipInfo.java
 *
 * This file is part of FareBot.
 * Learn more at: https://codebutler.github.io/farebot/
 *
 * Copyright (C) 2012, 2014-2015 Eric Butler <eric@codebutler.com>
 * Copyright (C) 2013 Wilbert Duijvenvoorde <w.a.n.duijvenvoorde@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.farebot.transit.ovc;

import android.os.Parcel;
import android.os.Parcelable;

import com.codebutler.farebot.util.Utils;

import java.util.Calendar;
import java.util.Date;

class OVChipInfo implements Parcelable {

    public static final Parcelable.Creator<OVChipInfo> CREATOR = new Parcelable.Creator<OVChipInfo>() {
        @Override
        public OVChipInfo createFromParcel(Parcel source) {
            int company = source.readInt();
            int expdate = source.readInt();
            Date birthdate = new Date(source.readLong());
            int active = source.readInt();
            int limit = source.readInt();
            int charge = source.readInt();
            int unknown = source.readInt();
            return new OVChipInfo(company, expdate, birthdate, active, limit, charge, unknown);
        }

        @Override
        public OVChipInfo[] newArray(int size) {
            return new OVChipInfo[size];
        }
    };

    private final int mCompany;
    private final int mExpdate;
    private final Date mBirthdate;
    private final int mActive;
    private final int mLimit;
    private final int mCharge;
    private final int mUnknown;

    private OVChipInfo(
            int company,
            int expdate,
            Date birthdate,
            int active,
            int limit,
            int charge,
            int unknown
    ) {
        mCompany = company;
        mExpdate = expdate;
        mBirthdate = birthdate;
        mActive = active;
        mLimit = limit;
        mCharge = charge;
        mUnknown = unknown;
    }

    OVChipInfo(byte[] data) {
        if (data == null) {
            data = new byte[48];
        }

        int company;
        int expdate;
        Date birthdate = new Date();
        int active = 0;
        int limit = 0;
        int charge = 0;
        int unknown = 0;

        company = ((char) data[6] >> 3) & (char) 0x1F; // Could be 4 bits though
        expdate = (((char) data[6] & (char) 0x07) << 11)
                | (((char) data[7] & (char) 0xFF) << 3)
                | (((char) data[8] >> 5) & (char) 0x07);

        if ((data[13] & (byte) 0x02) == (byte) 0x02) {
            // Has date of birth, so it's a personal card (no autocharge on anonymous cards)
            int year = (Utils.convertBCDtoInteger(data[14]) * 100) + Utils.convertBCDtoInteger(data[15]);
            int month = Utils.convertBCDtoInteger(data[16]);
            int day = Utils.convertBCDtoInteger(data[17]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            birthdate = calendar.getTime();

            active = (data[22] >> 5) & (byte) 0x07;
            limit = (((char) data[22] & (char) 0x1F) << 11) | (((char) data[23] & (char) 0xFF) << 3)
                    | (((char) data[24] >> 5) & (char) 0x07);
            charge = (((char) data[24] & (char) 0x1F) << 11) | (((char) data[25] & (char) 0xFF) << 3)
                    | (((char) data[26] >> 5) & (char) 0x07);
            unknown = (((char) data[26] & (char) 0x1F) << 11) | (((char) data[27] & (char) 0xFF) << 3)
                    | (((char) data[28] >> 5) & (char) 0x07);
        }

        mCompany = company;
        mExpdate = expdate;
        mBirthdate = birthdate;
        mActive = active;
        mLimit = limit;
        mCharge = charge;
        mUnknown = unknown;
    }

    public int getCompany() {
        return mCompany;
    }

    public int getExpdate() {
        return mExpdate;
    }

    public Date getBirthdate() {
        return mBirthdate;
    }

    public int getActive() {
        return mActive;
    }

    public int getLimit() {
        return mLimit;
    }

    public int getCharge() {
        return mCharge;
    }

    public int getUnknown() {
        return mUnknown;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mCompany);
        parcel.writeInt(mExpdate);
        parcel.writeLong(mBirthdate.getTime());
        parcel.writeInt(mActive);
        parcel.writeInt(mLimit);
        parcel.writeInt(mCharge);
        parcel.writeInt(mUnknown);
    }
}
