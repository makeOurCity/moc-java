# MoC Java

MoC client for Java

[![Java CI with Maven](https://github.com/makeOurCity/moc-java/actions/workflows/test.yml/badge.svg)](https://github.com/makeOurCity/moc-java/actions/workflows/test.yml)

## Usage

See [city.makeour.moc.examples](./src/main/java/city/makeour/moc/examples/)

## Development

### Testing

Set `xxxx.code-workspace` env and use it.

```json
{
  "java.test.config": {
    "env": {
      "TEST_COGNITO_USER_POOL_ID": "",
      "TEST_COGNITO_CLIENT_ID": "",
      "TEST_COGNITO_USERNAME": "",
      "TEST_COGNITO_PASSWORD": ""
    }
  }
}
```

```console
mvn clean install
mvn test
```
