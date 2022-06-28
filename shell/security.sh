#!/bin/sh

cd ../security

keytool -genkey -alias server -keyalg RSA -keypass password -keystore serverkeystore.jks -storepass password -dname "CN=FCT, OU=DI, L=ALMADA"

keytool -export -alias server -file serverkey.cer -keystore serverkeystore.jks -storepass password

keytool -import -v -trustcacerts -alias clientTrust -keypass password -file serverkey.cer -keystore clientcacerts.jks -storepass password

cp serverkeystore.jks ../server/security/serverkeystore.jks
cp serverkeystore.jks ../client/security/~clientcacerts.jks

cd ../shell
