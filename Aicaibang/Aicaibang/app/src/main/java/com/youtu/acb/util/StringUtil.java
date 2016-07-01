package com.youtu.acb.util;

import java.math.BigDecimal;

/*
 * 修改string 格式方法集合
 */
public class StringUtil {
    /*
     * 保留2位小数
     */
    public static String ContainTwoDigitsAfterDot(String str) {
        if (str == null)
            return "0";

        BigDecimal decimal = new BigDecimal(str);
        String temp = decimal.toPlainString();
        if (temp.contains(".") && temp.length() > (temp.indexOf(".") + 3)) {
            return temp.substring(0, temp.indexOf(".") + 3);
        } else {
            if (str.equals(""))
                return "0.00";
            else
                return temp;
        }
    }

    public static String BigdecimalTwo(String str) {
        if (str == null || str.length() == 0)
            return "";

        BigDecimal decimal = new BigDecimal(str);
        String temp = decimal.toPlainString();
        if (temp.endsWith(".00") || temp.endsWith(".0")) {
            return temp.substring(0, temp.lastIndexOf("."));
        } else {
            if (temp.contains(".") && temp.length() > (temp.indexOf(".") + 3)) {
                return temp.substring(0, temp.indexOf(".") + 3);
            } else {
                if (str.equals(""))
                    return "0.00";
                else
                    return temp;
            }
        }
    }

    /*
     * 保留4位小数
     */
    public static String ContainFourDigitsAfterDot(String str) {
        if (str == null)
            return "";

        BigDecimal decimal = new BigDecimal(str);
        String temp = decimal.toPlainString();
        if (temp.contains(".") && temp.length() > (temp.indexOf(".") + 5)) {
            return temp.substring(0, temp.indexOf(".") + 5);
        } else {
            if (str.equals("")) {
                return "0.0000";
            } else {
                if (temp.contains(".")) {
                    temp += "0000";
                    return temp.substring(0, temp.indexOf(".") + 5);
                } else {
                    return temp;
                }
            }
        }
    }

    /*
     * like x,xxx.xx
     */
    public static String FormatFloat(String str) {
        if (str == null)
            return "";

        boolean fu = false;
        if (str.contains("-")) {
            fu = true;
            str = str.replaceAll("-", "");
        }
        String returnStr = null;
        if (str.contains(".")) {
            String inte = str.substring(0, str.indexOf("."));
            if (str.length() >= (str.indexOf(".") + 3)) {
                returnStr = addcommor(inte) + str.substring(str.indexOf("."), str.indexOf(".") + 3);
            } else if (str.length() == str.indexOf(".") + 2) {
                returnStr = addcommor(inte) + str.substring(str.indexOf("."), str.indexOf(".") + 2) + "0";
            } else {
                returnStr = addcommor(inte) + ".00";
            }
        } else {
            try {
                int test = Integer.parseInt(str);
                returnStr = addcommor(str) + ".00";
            } catch (NumberFormatException e) {
                returnStr = str; // 存在中文－－like 万
            }
        }

        if (fu) {
            return "-" + returnStr;
        } else {
            return returnStr;
        }
    }

    /*
     * like x,xxx
     */
    public static String FormatFloatOrInt(String str) {
        if (str == null)
            return "";
        if (str.contains(".")) {
            String inte = str.substring(0, str.indexOf("."));
            if (str.length() >= (str.indexOf(".") + 3)) {
                return addcommor(inte) + str.substring(str.indexOf("."), str.indexOf(".") + 3);
            } else if (str.length() == str.indexOf(".") + 2) {
                return addcommor(inte) + str.substring(str.indexOf("."), str.indexOf(".") + 2) + "0";
            } else {
                return addcommor(inte) + ".00";
            }
        } else {
            return addcommor(str); // 整数
        }
    }

    /*
     * x,xxx.xxxx
     */
    public static String FormatFloatFour(String str) {
        if (str == null)
            return "";
        if (str.contains(".")) {
            String inte = str.substring(0, str.indexOf("."));
            if (str.length() >= (str.indexOf(".") + 5)) {
                return addcommor(inte) + str.substring(str.indexOf("."), str.indexOf(".") + 5);
            } else if (str.length() > str.indexOf(".") + 2) {
                return addcommor(inte) + str.substring(str.indexOf("."), str.length());
            } else if (str.length() == str.indexOf(".") + 2) {
                return addcommor(inte) + str.substring(str.indexOf("."), str.indexOf(".") + 2) + "0";
            } else {
                return addcommor(inte) + ".00";
            }
        } else {
            return addcommor(str); // 整数
        }
    }

    /*
     * 小于0.01 4位
     */
    public static String FormatAsWish(String str) {
        if (str == null)
            return "";
        str = new BigDecimal(str).toPlainString();
        if (str.contains(".")) {
            String inte = str.substring(0, str.indexOf("."));
            try {
                double haha = Double.parseDouble(str);
                if (haha < 0.01d) {
                    if (str.length() >= (str.indexOf(".") + 5)) {
                        return addcommor(inte) + str.substring(str.indexOf("."), str.indexOf(".") + 5);
                    } else {
                        return "0";
                    }
                } else {
                    if (str.length() > str.indexOf(".") + 2) {
                        return addcommor(inte) + str.substring(str.indexOf("."), str.indexOf(".") + 3);
                    } else if (str.length() == str.indexOf(".") + 2) {
                        return addcommor(inte) + str.substring(str.indexOf("."), str.indexOf(".") + 2) + "0";
                    } else {
                        return addcommor(inte);
                    }
                }
            } catch (NumberFormatException e) {
                return "0";
            }
        } else {
            return addcommor(str); // 整数
        }
    }

    /*
     * add ,
     */
    private static String addcommor(String str) {

        StringBuilder sb = new StringBuilder();
        String newStr = new String(str);

        if (newStr.length() > 3)
            return splitStr(newStr, sb).toString();
        else
            return str;
    }

    /*
     * digui
     */
    private static StringBuilder splitStr(String newStr, StringBuilder sb) {
        String temp = newStr.substring(newStr.length() - 3, newStr.length());
        newStr = newStr.substring(0, newStr.length() - 3);
        if (newStr.length() > 3) {
            splitStr(newStr, sb);
            sb.append("," + temp);
        } else {
            sb.append(newStr + "," + temp);
        }

        return sb;
    }

    /*
     * add 0 or .00
     */
    private String convertStr(String apr1) {
        if (apr1.contains(".")) {
            if (apr1.indexOf(".") + 2 == apr1.length())
                apr1 = apr1 + "0%";
            else {
                apr1 = apr1 + "%";
            }
        } else {
            apr1 = apr1 + ".00%";
        }

        return apr1;
    }

    /**
     * make phone num show like 111****2222
     */
    public static String coverPhoneNum(String phoneNum) {
        if (phoneNum == null) {
            return "";
        }
        if (phoneNum.length() == 11) {
            return phoneNum.substring(0, 3) + "****" + phoneNum.substring(7, 11);
        }
        return "";
    }


    /**
     * @param str
     * @return
     */
    public static String getStringOutE(String str) {
        BigDecimal bd = new BigDecimal(str);
        return bd.toPlainString();
    }

    /**
     * input:1234567890
     * output:**** **** **** 1234
     */
    public static String formatBankCard(String cardNum) {

        int length = cardNum.length();
        int beishu = length / 4;
        if (length % 4 == 0 && beishu > 0) {
            beishu--;
        }

        StringBuilder result = new StringBuilder();

        int count = 0;
        for (int i = 0; i < length; i++) {
            result.append(cardNum.charAt(i));
            if (i % 4 == 3 && count < beishu) {
                count++;
                result.append(" ");
            }
        }

        return new String(result);
    }


}
