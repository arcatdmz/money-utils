
REM ���̍s��PDF�t�@�C����u�����t�H���_���w�肷��
set EPOS_PDF_DIR=".\example"

REM ���̍s�œǂݎ��N���w�肷��
set EPOS_YEAR=2013

REM ���̍s�ŏo�͂����CSV�f�[�^��ۑ�����t�@�C�����w�肷��
set EPOS_OUT=".\epos%EPOS_YEAR%.csv"

set JAVA_CP="bin;lib\itextpdf-5.4.5.jar"

java -cp %JAVA_CP% EposPDFParser %EPOS_YEAR% %EPOS_PDF_DIR% 1>%EPOS_OUT%
