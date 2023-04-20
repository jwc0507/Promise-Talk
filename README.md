# 실전프로젝트

## 프로젝트 명 : BIP


<p align="center"><img src="https://user-images.githubusercontent.com/110073865/197106872-529c8866-01b5-474c-8397-3edf86906bf3.png"></p>

<p align="center">실제 배포사이트 [BIP](https://berryimportantpromise.com/intro) 를 클릭해서 확인해주세요. </p>

***

### 프로젝트간략 설명
> 더이상 약속을 깨트리지 마세요. 약속을 관리하고 또 친구와 공유하세요!

### BE 팀원
 - 이승환, 최진원, 김성재
###
api명세서 : https://www.notion.so/2de1c1447f4347e896223a9964c69463?v=4057a04afd194b5488036d538974dadf
***


## 4,5주차. 

### 주요 개발사항 
1. SSE를 통한 알림기능 프론트와 연결 ✔️
2. HTTPS로의 전환 ✔️
3. redis를 사용한 인증번호 관리 ✔️
4. Custom validation 적용 ✔️
5. 버그수정 (진행중)
6. 추가 알림 기능 구현 (진행중)
7. 성능 개선 사항 파악 (진행중)

4,5주에는 버그 수정에 전념하겠습니다.
 
## 트러블 슈팅

#### 1. [🟢해결완료] SSE의 사용

 - 문제 사항 : 
   1. 프론트에게서 토큰을 받아와 유저정보를 빼서 해당 유저를 구독시켜야하지만 eventsource에 헤더를 넣을 수 없었음
   2. eventsource에 프론트에서 헤더로 토큰을 받을 수 없으므로 queryString 방식을 활용하게 되어 유저 구독에 id가 그대로 노출이 되게 됨.
 - SOLVE : eventsource polyfill로 헤더를 담아 호출. 정상적으로 토큰을 받아올 수 있었음.
 - 해결 완료.
 
 #### 2. [🟢해결완료] nginx를 사용하여 발생한 sse기능 이슈
 
 - 문제 사항 : 
   1. 보안성과 추후 서버증설이 있다면 로드밸런싱도 활용해보기 위해 nginx를 사용하여 ws를 구축하여 https를 적용해보려 시도함.
   2. nginx 적용 이후 sse기능이 정상동작하지 않게 됨. 
   3. FE에서 connect가 뜬뒤 1번 상태를 반환하며 더이상 알림을 수신받지 못하는 상황 발생.
 - 문제 파악 : 
   1. 서버에서는 정상적으로 구독을 한뒤 해당 멤버에 알림보내기를 시도중. connection이 close되는 상태인지 확인.
   2. nginx를 경유해서 통신을 할경우 프록시모듈의 기본 프로토콜이 http 1.0임. 이 프로토콜은 connection 헤더가 없거나 헤더가 close일 경우 단기 커넥션을 시도함. 
   단기 커넥션으로 sse연결을 할 경우 서버에서 메세지를 전송하지 못함.
 - SOLVE : 설정파일에 프록시 버전을 1.1로 명시하고, connection header를 비워주어 해결시도.
 - 해결완료.
 
 #### 3. [🟢해결완료] HikariCP DeadLock문제
 
 - 문제 사항 :
   1. hikaripool-1 - connection is not available request timed out after 30000ms
   2. SSE를 사용하고 나서 테스트중 어느 시점 이후부터 해당 오류로그가 잡히며 서비스 요청SQL들이 실행되지 않게 됨.
 - 문제 파악 : 
   1. 해당 오류로그가 어느 상황에서 발생하는지 파악
   2. 해당 에러사항은 spring 2.x 이후부터 기본 jdbc connection pool로 사용되는 HikariCP의 deadlock문제임. 
   3. 서로 다른 스레드에서 한정된 자원을 사용하려 할 때 먼저 선점한 스레드가 자원을 사용하고 다시반환하면 다음 스레드가 이용하는 방식을 사용할 수 있음.
 이 방식을 사용하는 과정에서 이전 스레드가 자원을 반환하지 않는다면 뒤에 대기중이던 스레드는 자원을 할당받지 못하므로 병목이 발생하게 됨. 이것을 deadlock이라부름.
   4. 이 오류사항은 connection pool의 용량을 초과한 대기가 발생해서 생기는 문제임. 
 - SOLVE :
   1. connection pool을 늘린다 : 이 방법은 임시적인 해결방안은 될 수 있겠지만 병목이 발생하는 주 원인이 해결되지 않았으므로 해결방안이 아님.
   2. OSIV를 false로 설정 : 기존 default로 true로 되어있던 OPEN SESSION IN VIEW를 false로 설정하여 영속성컨텍스트를 가지지 않도록 하여 jdbc connection을 가지지 않도록 함.
 - 해결 완료
 
 
 #### 4. [🟢해결완료] nginx를 사용하며 생기는 ec2 서버의 지연현상
 
 - 문제 사항 : 
   1. nginx를 사용하고 난 이후 ec2서버의 응답처리 속도가 점점 느려지기 시작함.
 - 문제 파악 : 기능이 느려지는 것이지 작동불능이 아니기때문에 예외처리가 발생하지 않아 ec2서버의 로그확인이 불가능했음. 프리티어 서버의 자원이슈로 가정하고 nginx를 사용하지 않고 was에서 직접 ssl인증을 받는 방식으로 변경하기로 함.
 - SOLVE : keystore.p12파일 생성 후 파일경로를 참조시키고 재실행
   - 해결완료
 
 #### 5. [🟢해결완료] 네이버로그인 사용 검수 반려

 - 문제 사항 : 네이버도 카카오와 동일하게 전화번호의 수집요청에 사업자 등록증이 필요하다는 검수결과를 받았음.
 - SOLVE : 카카오와 동일하게 로그인 이후에 전화번호 인증을 받아 전화번호를 집어넣게 했음.


***


## 3주차. 

### 주요 개발사항
1. SSE를 통한 알림 스케쥴링 구현(테스트완료) - 현재 프론트에선 작업진행 보류중 ✔️
2. 재능기부 게시판 및 댓글 CRUD ✔️
3. 토큰 재발급 기능 ✔️

+ 신고하기 기능 추가중..
+ 마이페이지 글 보기 추가중...
+ 포인트 인계 알고리즘 설계중..

발표가 있는 주차 이므로 트러블 슈팅에 집중해봅시다.
자잘한 수정사항은 커밋을 참조해주세요.
 
## 트러블 슈팅

#### 1. [🟢해결완료] SSE의 사용 - FCM, 카카오 알림톡 에서 SSE로 방향을 선회함.

 - 제약 사항 : 카카오 알림톡은 비즈니스 계정이 필요함, 또한 알림을 위해 비즈니스 톡을 쓰는 것은 적합하지 않은 것 같음. FCM은 웹 푸시알림이라 현재 푸시알림을 구현하려던 것이 아니며
일반 웹사이트에 접속중일때 알림을 하는것을 구현하는 것이 목표였으므로 SSE를 적용하기로 했음. 
 - SOLVE : SSE를 통해 서버에서 구독한 클라이언트로 데이터를 보내는 알고리즘을 테스트 완료함. FCM도 추후 사용용도가 있을지도몰라서 구현된 코드를 주석처리하여 환경변수와 남겨둠.
 
 #### 2. [🟢해결완료] 카카오 전화번호 관련 이슈 

 - 제약 사항 : 기본적으로 카카오에서 유저의 전화번호를 가져오려면 사업자등록이 필요함(비즈니스계정). 사이트에서 전화번호를 메인으로 사용할 생각이므로 카카오 연동이후 전화번호 추가 입력이 필요하게 되어 그 과정에서 기존에 전화번호로 가입한 계정이 있다면 전화번호 입력시에 카카오 계정과 연동을 시키도록 구현해야 했음.
 - SOLVE :  만약 기생성된 전화번호 계정이 있다면 카카오 계정의 필수정보들만 가져와 기생성된 계정에 넣고 카카오 계정을 자동삭제하고 동시에 기존계정을 로그인하도록 재 설계함. 
 
 #### 3. [🟢해결완료] onetoone관계에서 생긴 이슈와 처리법

 - 에러 사항 : More than one row with the given identifier was found
 - SOLVE :
   - 해당 오류가 제보된 뒤 시간이 지나자 서비스가 점점 심하게 마비되기 시작하여 로그분석 시작.
   - 광범위한 호출부분에서 해당 예외가 발생중이었으며 기본적으로 Member객체의 연관관계가 문제인 것을 인식.
   - 첫 에러제보가 발생한 시점 바로 이전의 커밋사항중 엔티티의 컬럼, 또는 속성에 변동사항이 있는지 체크
   - 의심되는 컬럼변경 사항을 발견 OneToOne friendlistfriend 양방향 구조
   - 해당 컬럼의 추가 이유 파악 
     1. 친구서비스에서 일방적인 친구관계를 만들기로 했음. 누군가가 자신을 친구로 추가하더라도 그사람에게만 친구 등록이 되고 나에게는 실제 친구가 아닌 추천친구로만 보일 수 있게 구현.
     2. 친구목록 설계할때 친구관계의 주인과 대상 친구, 둘을 테이블에 넣기로했고 둘다 Member객체에서 연관관계를 맺음.
     3. 친구관계의 주인만 양방향 설계를 하여 Member객체가 삭제될 때 해당 친구목록이 같이 지워질 수 있게 함.
     4. 추후 자신이 대상 친구일 경우에도 해당 친구관계를 지워야했기 때문에 삭제 오류가 발생하여 해당 필드에도 양방향 연결 후 cascade 옵션을 추가 함.
   - 하나의 엔티티가 같은 엔티티를 외래키로 두번 참조해서 생기는 문제로 인식.
   - 둘중 조금 더 중요한 owner(친구관계의 주인)을 남기고 대상 친구를 참조하는 Member의 관계 컬럼을 삭제시켜 단방향으로 변경.
   - cascade기능(자신을 대상친구로 하는 것 기준 친구목록 삭제)을 하는 메소드를 만들어서 맴버객체가 삭제 되는 시점의 부분들에 해당 메소드를 먼저 실행시키도록 함.
   - 해결 완료.
 
 #### 4. [🟢해결완료] equestDto를 사용할때 key를 잘못 입력해서 보내면 값을 받지못한 dto의 값이 null이 되어 들어오게 됨.

 - 제약 사항 : 원래 예상은 dto의 값이 null이면 에러를 내보낼 줄 알았지만 그러지 않았음. 결국 null인 닉네임으로 member를 찾는 jpa가 호출되었으며 테스트를 위해 저장되어있던 닉네임이 null인 유저가 실제로 찾아져서 관계구조가 맺어짐으로써 서버가 불안정해짐. 
 - SOLVE : 
   - NoArgsConstructor, AllArgsConstructor을 dto에서 사용해보기 : AllArgs를 사용했을때 null로 값을 받아오면 에러를 내보내게 되었음.
하지만 실제 기능로직이 정상작동하지 않게 됨.
   - vaild어노테이션을 이용하기 : dto의 값에 vaildation을 적용시키기로함. 모든 dto필드에 @NotBlank를 달아서 테스트.
     -  No validator could be found for constraint 'javax.validation.constraints.NotBlank' validating type 'java.lang.Integer' 발생
     - integer타입은 null로 체크해야함. integer타입들은 @NotNull을 사용하도록 변경 후 해결
   - 해결완료
 
 #### 5. [🟢해결완료]  subject를 id로 해서 생긴 문제와 그 처리법

 - 문제 사항 : 토큰발급의 subject를 멤버의 primary key인 id로 해서 db클리어 후 빌드되었을 때 세션스토리지에 토큰이 남아있다면 비정상적으로 로그인상태가 유지되거나 다른사람의 계정으로 접속이 가능하던 점
 - SOLVE : 어플리케이션의 pid를 가져와서 jwt secretkey와 함께 사용함. 매 빌드마다 시크릿키가 바뀌므로 토큰이 무효화되어 로그인 상태가 아니게되어 정상적으로 작동하게됨.


***

## 2주차. 

### 주요 개발사항
1. FCM OR KAKAO 알림톡을 이용한 알림구현 🔧
2. STOMP를 이용한 채팅기능 구현 ✔️
3. 카카오 지도 API를 활용한 마커기능 추가. ✔️

9월 26일까지 지난주 토요일 회의에서 기능구현 하기로 한 부분구현 (약속 완료 api외 4개 api) ✔️
+날씨호출 api추가 ✔️
+약속알림 스케쥴링 로컬테스트 완료 ✔️

이번 주도 힘내주세요!
 
## 트러블 슈팅

#### 1. [🟢해결완료] 신용도나 포인트가 중간중간 값이 0이 되버리는 경우가 보임

 - 에러 사항 : 가끔 DB의 값들을 모니터링 할 때 포인트나 신용도가 중간중간 0이 되버리는 버그가 있는 것을 확인했음 (9월 29일 기준 DB클리어 이후 보이진 않음)
 - SOLVE : DB가 수정되지 않아 생긴 오류일 것으로 보이므로, DB클리어 시점에서 해결 된 것같지만 지속적인 모니터링으로 해당현상의 정확한 원인 파악이 필요함.
 
 #### 2. [🟢해결완료] 서버시간 관련 이슈

 - 제약 사항 : EC2서버를 사용중이므로 서버에 올라간 서비스의 시간이 UTC로 고정되어 약속알고리즘을 돌렸을때 한국시간기준이 아닌 UTC기준으로 결과들이 출력되게 됨. (남은시간, 체크인불가능 등의 부분)
 - SOLVE :  코드부분에서 타임존을 세팅하는 구문이 작동하지 않아서, 서버 자동배포시 실행구문에 시간설정구문을 추가하여 해결함.
 
 #### 3. [🟢해결완료] websocket connection to failed connection closed to (ws요청시 서버는 열려있지만 서버까지 요청이 오지 않던 경우)

 - 에러 사항 : WS을 사용해서 클라이언트에서 메세지를 받기위해 WS서버 요청을 할 때 서버가 꺼져있다는 오류문구 출력
 - SOLVE : 호출 구문의 맨뒤에 /websocket을 붙여서 호출하면 정상 작동함.
 
 #### 4. [🟢해결완료] 기상청 날씨api사용할때 http error가 간헐적으로 발생하던 사항 

 - 제약 사항 : 같은코드를 사용하는데 정상응답이 올때도 있고 에러를 띄우는 경우도 있는 것을 확인함
 - SOLVE : 요청에 대기시간이 있는 줄 알았는데 요청서버쪽의 불안정이 이유였음. 최종적으로 날씨한번 조회에 2분30초가 걸리거나 요청에 500에러를 띄우며 게이트웨이 오류를 띄우는 것을 보고 해당 api사용을 포기하고 open weather api를 사용하기로 함.
 
 #### 5. [🟢해결완료] 약속체크인생성 class java.lang.string cannot be cast to class com 에러

 - 에러 사항 : 약속을 만들고 약속의 체크인리스트를 만드는 과정에서 event가 필요하므로 컨트롤러에서 다시 체크인리스트를 만드는 메소드를 호출하게 함. 그 과정에서 먼저 넘어왔던 responseDto의 데이터를 강제형변환한 뒤 id를 추출하여 사용하려했는데 이 부분에서 casting 에러가 발생함.
 - SOLVE : 컨트롤러로 돌아와서 responseDto내용을 꺼내 형변환 한뒤 다시 메소드 호출하는 로직을 없애고 flow가장 마지막에 위에서 작업한 repository를 flush한 뒤 생성 메소드롤 호출시켜 처리함.

#### 6. [🟢해결완료] 특정 엔티티(EventSchedule)의 테이블이 생성되지 않음

- 에러 사항 : 서버는 구동되지만 특정 엔티티의 테이블이 생성되지 않아 해당 엔티티와 관련된 로직이 구동되지 않음. 이러한 문제는 H2를 사용하였을 때는 발생하지 않았으나 MySQL를 사용했을 시 나타남.
- SOLVE : EventSchedule 엔티티 내에 Before라는 이름의 필드가 존재했는데, 이 이름이 MySQL 상에서 예약어로 등록되어있던 단어였기에 테이블 생성 과정에서 오류가 있던 것으로 확인함. 따라서 해당 필드의 이름을 예약어와 중복되지 않는 단어로 교체한 결과 정상적으로 테이블 생성되었음.

***

## 1주차. 

### 주요 개발사항
1. DB ✔️
2. api설계 ✔️
3. 기본 기능들 구현 ✔️

9월 24일까지 로그인,회원가입,약속CRUD,친구페이지 기능 구현 완료 목표 ✔️

#### 레포지토리 클론해갔을때 resourse 경로 아래에 application-aws.properties 파일 생성필수. 
내용은 개인 DM으로 드리겠습니다.

## 트러블 슈팅

#### 1. [🟢해결완료] unable to evaluate the expression method threw 'org.hibernate.lazyinitializationexception' exception

 - 에러 사항 : 엔티티 컬럼 LAZY 이슈
 - SOLVE : 엔티티 객체를 불러올때 지연로딩을 해서 실제 사용할때 값을 알 수 없어 에러가 발생. 해당 ENTITIY만 EAGER로 변경.

#### 2. [🟢해결완료] org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags 

- 에러 사항 : 두개의 EAGER사용제약
- SOLVE : 양방향 사용을 위해 EAGER 컬럼을 두개 생성하여 제약사항에 걸림. 정말 필요한 컬럼을 남기고 단방향 설계로 수정.

#### 3. [🟢해결완료] 카카오 로그인 구현시 넘어오는 값으로 회원가입 구현불가

- 제약 사항 : 카카오 api에서 넘어온 info에 유저인증 식별자로 사용하고 있던 전화번호가 들어있지 않음. 회원가입과 로그인 할 때 토큰생성 불가능.
- SOLVE : 유저인증을 기본키 id로 할 수 있게 변경함. 변경함으로써 닉네임이나 전화번호등 겉으로 드러나는 정보가 수정될 때 토큰 재생성을 할 필요가 없음

#### 4. [🟢해결완료] oauth로그인했을때 토큰인증이 필요한 api호출시 401에러 (인증실패)를 반환

- 에러 사항 : 로컬테스트로 코드 테스트 결과 소셜로그인회원의 로그인이후 securitycontextholder에 jwt토큰을 같이 넣어서 유저정보를 넣지 않던 것을 확인, 
추가로 header prefix형식도 기존 filter에서 체크하는 형식과도 같지 않던 부분 확인함.
- SOLVE : 토큰 생성부분을 contextholder에 유저정보를 넣는 부분보다 앞으로 당겨줌. 토큰 생성부에서 붙이는 prefix값을 BEARER에서 Bearer로 수정해줌. (이 부분은 filter에서 prefix부분만 uppercase 또는 lowercase형식으로 모두 받을 수 있게 수정해도 괜찮을 것 같음) 

#### 5. [🟢해결완료] 네이버 로그인할때 로컬에서 로그인이 진행되지만 프론트에서 허용된 사이트가 아니라 로그인 할 수 없던 부분.

- 에러 사항 : 프론트의 개발환경 (localhost:3000)에서 로그인 요청이 허용되지 않던 점
- SOLVE : api web서비스 부분을 잘못 이해하고 있었음. be의 서버주소를 적는 것이 아닌 실제 서비스가 동작할 부분의 주소를 적어야함. localhost:3000을 서비스 url로 지정해주어 해결함.

![image](https://user-images.githubusercontent.com/80738030/233302495-990304c7-1d45-4faa-af3b-4d1a3d77be21.png)
