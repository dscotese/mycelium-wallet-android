/*
 * Copyright 2013, 2014 Megion Research & Development GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mrd.bitlib.util;

import com.megiontechnologies.Bitcoins;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;

/**
 * Utility for turning an amount of satoshis into a user friendly string. 1
 * satoshi == 0.000000001 BTC
 */
public class CoinUtil {
   private static final BigDecimal BTC_IN_SATOSHIS = new BigDecimal(100000000);
   private static final BigDecimal mBTC_IN_SATOSHIS = new BigDecimal(100000);
   private static final BigDecimal uBTC_IN_SATOSHIS = new BigDecimal(100);
   private static final BigDecimal BITS_IN_SATOSHIS = new BigDecimal(100);

   private static final DecimalFormat COIN_FORMAT;

   static {
      COIN_FORMAT = new DecimalFormat();
      COIN_FORMAT.setGroupingSize(3);
      COIN_FORMAT.setGroupingUsed(true);
      COIN_FORMAT.setMaximumFractionDigits(8);
      DecimalFormatSymbols symbols = COIN_FORMAT.getDecimalFormatSymbols();
      symbols.setDecimalSeparator('.');
      symbols.setGroupingSeparator(' ');
      COIN_FORMAT.setDecimalFormatSymbols(symbols);
   }

   public enum Denomination {
      UNIT(8, "", "",  BTC_IN_SATOSHIS),
      MILLI(5, "m", "m", mBTC_IN_SATOSHIS),
      MICRO(2, "u", "\u00B5", uBTC_IN_SATOSHIS),
      BITS(2, "bits", "bits", BITS_IN_SATOSHIS);

      private final int _decimalPlaces;
      private final String _asciiString;
      private final String _unicodeString;
      private final BigDecimal _oneUnitInSatoshis;

      Denomination(int decimalPlaces, String asciiString, String unicodeString, BigDecimal oneUnitInSatoshis) {
         _decimalPlaces = decimalPlaces;
         _asciiString = asciiString;
         _unicodeString = unicodeString;
         _oneUnitInSatoshis = oneUnitInSatoshis;
      }

      public String getUnicodeString(String symbol) {
         if (symbol.contains("BTC") || symbol.contains("BCH")) {
            switch (this) {
               case UNIT:
                  return symbol;
               case MILLI:
               case MICRO:
                  return this.getUnicodeName().substring(0, 1) + symbol;
               default:
                  return getUnicodeName();
            }
         } else {
            return symbol;
         }
      }

      public int getDecimalPlaces() {
         return _decimalPlaces;
      }

      public String getAsciiName() {
         return _asciiString;
      }

      public String getUnicodeName() {
         return _unicodeString;
      }

      public BigDecimal getOneUnitInSatoshis() {
         return _oneUnitInSatoshis;
      }

      public static Denomination fromString(String string) {
         if (string == null) {
            return UNIT;
         }
         switch (string) {
            case "BTC"://back compatibility
            case "unit":
               return UNIT;
            case "mBTC"://back compatibility
            case "milli":
               return MILLI;
            case "uBTC": //back compatibility
            case "micro":
               return MICRO;
            case "bits":
               return BITS;
            default:
               return UNIT;
         }
      }
   }

   /**
    * Number of satoshis in a Bitcoin.
    */
   public static final BigInteger BTC = new BigInteger("100000000", 10);

   /**
    * Get the given value in satoshis as a string on the form "10.12345"
    * denominated in BTC.
    * <p>
    * This method only returns necessary decimal points to tell the exact value.
    * If you wish to display all digits use
    * {@link CoinUtil#fullValueString(long)}
    * 
    * @param value
    *           The number of satoshis
    * @param withThousandSeparator
    *           Use ' ' as the 1000 grouping separator in the output 
    * @return The given value in satoshis as a string on the form "10.12345".
    */
   public static String valueString(long value, boolean withThousandSeparator) {
      return valueString(value, Denomination.UNIT, withThousandSeparator);
   }

   /**
    * Get the given value in satoshis as a string on the form "10.12345" using
    * the specified denomination.
    * <p>
    * This method only returns necessary decimal points to tell the exact value.
    * If you wish to display all digits use
    * {@link CoinUtil#fullValueString(long, Denomination)}
    * 
    * @param value
    *           The number of satoshis
    * @param denomination
    *           The denomination to use
    * @param withThousandSeparator
    *           Use ' ' as the 1000 grouping separator in the output 
    * @return The given value in satoshis as a string on the form "10.12345".
    */
   public static String valueString(long value, Denomination denomination, boolean withThousandSeparator) {
      BigDecimal d = BigDecimal.valueOf(value);
      d = d.divide(denomination.getOneUnitInSatoshis());
      if (!withThousandSeparator) {
         return d.toPlainString();
      } else {
         return COIN_FORMAT.format(d);
      }
   }

   /**
    * Get the given value as a string on the form "10.12345" using
    * the specified denomination.
    * <p>
    * This method only returns necessary decimal points to tell the exact value.
    * If you wish to display all digits use
    * {@link CoinUtil#fullValueString(long, Denomination)}
    *
    * @param value
    *           The number of bitcoins
    * @param denomination
    *           The denomination to use
    * @param withThousandSeparator
    *           Use ' ' as the 1000 grouping separator in the output
    * @return The given value as a string on the form "10.12345".
    */
   public static String valueString(BigDecimal value, Denomination denomination, boolean withThousandSeparator) {
      Long satoshis = Bitcoins.nearestValue(value).getLongValue();
      return valueString(satoshis, denomination, withThousandSeparator);
   }

   private static HashMap<Integer, DecimalFormat> formatCache = new HashMap<Integer, DecimalFormat>(2);

   /**
    * Get the given value in satoshis as a string on the form "10.12345" using
    * the specified denomination.
    * <p>
    * This method only returns necessary decimal points to tell the exact value.
    * If you wish to display all digits use
    * {@link CoinUtil#fullValueString(long, Denomination)}
    *
    * @param value
    *           The number of satoshis
    * @param denomination
    *           The denomination to use
    * @param precision
    *           max number of digits after the comma
    * @return The given value in satoshis as a string on the form "10.12345".
    */
   public static String valueString(long value, Denomination denomination, int precision) {
      BigDecimal d = BigDecimal.valueOf(value);
      d = d.divide(denomination.getOneUnitInSatoshis());

      if (!formatCache.containsKey(precision)){
         DecimalFormat coinFormat = (DecimalFormat) COIN_FORMAT.clone();
         coinFormat.setMaximumFractionDigits(precision);
         formatCache.put(precision, coinFormat);
      }
      return formatCache.get(precision).format(d);
   }

   /**
    * Get the given value in satoshis as a string on the form "10.12345000"
    * denominated in BTC.
    * <p>
    * This method always returns a string with 8 decimal points. If you only
    * wish to have the necessary digits use {@link CoinUtil#valueString(long, boolean)}
    * 
    * @param value
    *           The number of satoshis
    * @return The given value in satoshis as a string on the form "10.12345000".
    */
   public static String fullValueString(long value) {
      return fullValueString(value, Denomination.UNIT);
   }

   /**
    * Get the given value in satoshis as a string on the form "10.12345000"
    * using the specified denomination.
    * <p>
    * This method always returns a string with all decimal points. If you only
    * wish to have the necessary digits use
    * {@link CoinUtil#valueString(long, Denomination, boolean)}
    * 
    * @param value
    *           The number of satoshis
    * @param denomination
    *           The denomination to use
    * @return The given value in satoshis as a string on the form "10.12345000".
    */
   public static String fullValueString(long value, Denomination denomination) {
      BigDecimal d = BigDecimal.valueOf(value);
      d = d.movePointLeft(denomination.getDecimalPlaces());
      return d.toPlainString();
   }

}
