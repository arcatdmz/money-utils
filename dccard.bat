
REM 次の行でCSVファイルを置いたフォルダを指定する
set DCCARD_CSV_DIR=".\example"

REM 次の行で読み取る年を指定する
set DCCARD_YEAR=2013

REM 次の行で出力されるCSVデータを保存するファイルを指定する
set DCCARD_OUT=".\dccard%DCCARD_YEAR%.csv"

set JAVA_CP="bin"

java -cp %JAVA_CP% DCCardCSVParser %DCCARD_YEAR% %DCCARD_CSV_DIR% 1>%DCCARD_OUT%
