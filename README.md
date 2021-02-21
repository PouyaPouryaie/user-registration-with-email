# user-registration-with-email

signUp user, and then send email with maildev, <br >
then confirm email, and enabled user.

### maildev
install and run maildev
```
$ npm install -g maildev
$ maildev
```
### Run
```
mvn spring-boot:run
```
### CURL
```
curl --location --request POST 'localhost:8080/api/v1/registration' \
--header 'Content-Type: application/json' \
--data-raw '{
    "firstName": "pouya",
    "lastName": "pouryaie",
    "email": "hellow@amigoscode.com",
    "password": "password"
}'
```