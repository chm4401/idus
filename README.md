# idus
idus test
## KST 기준 daily partition 처리
## 재처리 후 parquet, snappy 처리
## External Table 방식으로 설계
- s3에 업로드하는 로직으로 spark application을 작성해 두었습니다.
- Hive External Table 방식으로 테이블을 생성하는 코드를 작성해 두었으며, SELECT 문을 통해 확인 가능하게 임시 코드를 넣어두었습니다.
## 추가 기간 처리에 대응가능하도록 구현
- "추가 기간 처리"에 대응하기 위해 날짜(일반적으로 배치에 날짜 arg를 사용하므로)를 arg로 받아서, 적절한 파일명을 찾게 작성하였습니다. <br>
- 2019-Nov.csv와 같은 배치 처리에 필요한 csv 파일은 최상단 폴더에 미리 다운로드 받아져 있다는것이 전제조건입니다.
## 배치 장애시 복구를 위한 장치 구현
- 일반적으로 airflow 등의 스케쥴링 도구로 배치 장애시 복구를 진행할 것으로 생각하나, Spark Application으로 과제가 주어져 <br> spark-submit시
`--conf "spark.task.maxFailures=4" --conf "spark.stage.maxConsecutiveAttempts=4"` 옵션을 주었습니다. <br>
과제 조건이 의도한 바와 맞는지 불명확하여, 해당 설정이 기본값과 동일하지만, 명시하기 위해 넣어두었습니다.
## 기타
- Spark job은 아래와 같이 실행하였습니다
 ```
spark-submit ^
  --class Main ^
  --master local[8] ^
  --executor-memory 16G ^
  --driver-memory 16G ^
  --conf "spark.executor.extraJavaOptions=-Dlog4j.logger.org.apache.spark=ERROR" ^
  --conf "spark.driver.extraJavaOptions=-Dlog4j.logger.org.apache.spark=ERROR" ^
  --conf "spark.task.maxFailures=4" ^
  --conf "spark.stage.maxConsecutiveAttempts=4" ^
  Idus-assembly-0.1.jar 2019-11-01
```
- sbt assembly 결과 jar는 용량 문제로 업로드하지 못했습니다.
- 2019-Nov.csv는 루트 폴더(build.sbt 위치)에 존재하는 기준으로 작성했습니다.
- 실제 실행한 access_key 및 file_path는 비식별화 하였습니다.
