
REM ���̍s��CSV�t�@�C����u�����t�H���_���w�肷��
set DCCARD_CSV_DIR=".\example"

REM ���̍s�œǂݎ��N���w�肷��
set DCCARD_YEAR=2013

REM ���̍s�ŏo�͂����CSV�f�[�^��ۑ�����t�@�C�����w�肷��
set DCCARD_OUT=".\dccard%DCCARD_YEAR%.csv"

set JAVA_CP="bin"

java -cp %JAVA_CP% DCCardCSVParser %DCCARD_YEAR% %DCCARD_CSV_DIR% 1>%DCCARD_OUT%
