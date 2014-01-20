
REM 次の行でPDFファイルを置いたフォルダを指定する
set EPOS_PDF_DIR=".\example"

REM 次の行で読み取る年を指定する
set EPOS_YEAR=2013

REM 次の行で出力されるCSVデータを保存するファイルを指定する
set EPOS_OUT=".\epos%EPOS_YEAR%.csv"

set JAVA_CP="bin;lib\itextpdf-5.4.5.jar"

java -cp %JAVA_CP% EposPDFParser %EPOS_YEAR% %EPOS_PDF_DIR% 1>%EPOS_OUT%
