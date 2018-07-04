/*
 * MIT License
 *
 * Copyright (c) 2017-2018 talust.org talust.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.talust.chain.block.test;


import org.talust.chain.account.Account;
import org.talust.chain.account.AccountType;
import org.talust.chain.common.crypto.Base58;
import org.talust.chain.common.crypto.Hex;
import org.talust.chain.storage.AccountStorage;

//对一些帐户进行初始化
public class AccountTest {

    public static void main(String[] args) throws Exception{
        AccountStorage accountStorage = AccountStorage.get();
        accountStorage.init();
        Account account = new Account();
        account.setAccount("root");
        account.setAccPwd("rootpwd");
        account.setAccType(AccountType.USER.getType());

//        boolean hadAccount = accountStorage.hadAccount();
//        if(hadAccount){
//            System.out.println("已经存在帐户信息...");
//        }else{
//            accountStorage.createAccount(account);
//        }
        System.out.println("public:"+ Hex.encode(account.getAddress()));
        System.out.println("address:"+ Base58.encode(account.getAddress()));




    }

}
