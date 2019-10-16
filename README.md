# run-env [![CircleCI](https://circleci.com/gh/hden/run-env/tree/master.svg?style=svg)](https://circleci.com/gh/hden/run-env/tree/master)

Manage multiple deployment profiles for Google Cloud Run.

## Usage

Cloud Run's cost model fits very well with multiple staging environments.
For example, you might want to deploy multiple services with slightly different
environmental variables (e.g. different Cloud SQL connection). Although Google
offers a friendly UI, it's not very helpful when you need to manage multiple
profiles within a CI/CD pipeline.

Here is an example of running two environments.

```dev.json
{
  "name": "hello",
  "image": "gcr.io/cloudrun/hello",
  "platform": "managed",
  "concurrency": 10,
  "max-instances": 1,
  "memory": "512Mi",
  "region": "asia-northeast1",
  "env": {
    "MYSQL_USER": "local",
    "MYSQL_PASSWORD": "berglas://my-bucket/mysql-password-dev"
  },
  "cloudsql-instances": [
    "staging"
  ]
}
```

When you want to override some, but not all parameters, run-env can automatically
read from other profiles using the **include** key.

```prod.json
{
  "include": ["dev.json"],
  "max-instances": 10,
  "allow-unauthenticated": true,
  "env": {
    "MYSQL_USER": "production",
    "MYSQL_PASSWORD": "berglas://my-bucket/mysql-password-prod"
  },
  "cloudsql-instances": [
    "production"
  ]
}
```

Several feature that might be extra helpful:

* You can **include** multiple files.
* You can nest multiple files.
* When used with tools like [berglas](https://github.com/GoogleCloudPlatform/berglas), the sensitive information can be safely encrypted and stored, outside of your git repository.
* Text fields supports shell-like interpolation so you can dynamically configure service name (e.g. `prefix-$CIRCLE_BRANCH`).

## License
MIT
