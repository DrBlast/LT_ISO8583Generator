chcp 1251
echo "%DATE%"
java  -Dname=LT_ISO8583Generator -Xms24m -Xmx256m -jar bin/artifacts/LT_ISO8583Generator_2.4.0_EN.jar
pause