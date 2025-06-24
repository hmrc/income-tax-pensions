
# income-tax-pensions
This is where we make API calls from users viewing and making changes to the Pensions section of their income tax return.

## Running the service locally

You will need to have the following:

### Service Manager 
Installed/configured [service manager](https://github.com/hmrc/sm2).  
The service manager profile for this service is:
```shell
  sm2 --start INCOME_TAX_PENSIONS
```

### Sbt
```shell
  sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```

Run the following command to start the remaining services locally:

```shell
  sudo mongod (If not already running)
  sm2 --start INCOME_TAX_SUBMISSION_ALL
```

This service runs on port: `localhost:9322`

### Pensions endpoints:

**GET     /income-tax/nino/:nino/sources**                (Gets the Pensions data for this user)

**GET     /pension-charges/nino/:nino/taxYear/:taxYear** (Retrieves the pension charges for a given nino and tax year)

**DELETE  /pension-charges/nino/:nino/taxYear/:taxYear** (Deletes the pension charges for a given nino and tax year)

**PUT     /pension-charges/nino/:nino/taxYear/:taxYear** (Creates or updates one or more types of pension charges)

**GET     /pension-reliefs/nino/:nino/taxYear/:taxYear** (Retrieves the pension reliefs for a given nino and tax year)

**PUT     /pension-reliefs/nino/:nino/taxYear/:taxYear** (Creates or updates on one or more types of pension reliefs)

**DELETE  /pension-reliefs/nino/:nino/taxYear/:taxYear** (Deletes the pension reliefs for a given nino and tax year)

**GET     /pension-income/nino/:nino/taxYear/:taxYear** (Retrieves the pension income for a given nino and tax year)

**PUT     /pension-income/nino/:nino/taxYear/:taxYear** (Creates or updates on one or more types of pension income)

**DELETE  /pension-income/nino/:nino/taxYear/:taxYear** (Deletes the pension income for a given nino and tax year)

### Downstream services
All Pensions data are retrieved / updated via this downstream system.
- DES (Data Exchange Service)

### Connected microservices
This service connects to [income-tax-benefits](https://github.com/hmrc/income-tax-benefits) for state benefits data, which also ultimately calls the above downstream service.


## Ninos with stub data for Pensions

### In-Year
| Nino      | Pensions data                                                 |
|-----------|---------------------------------------------------------------|
| AA370343B | User with pension reliefs, pension charges and state benefits |
| AA123459A | User with pension reliefs, pension charges and state benefits |

### End of Year
| Nino      | Pensions data                                                 |
|-----------|---------------------------------------------------------------|
| AA370343B | User with pension reliefs, pension charges and state benefits |
| AA123459A | User with pension reliefs, pension charges and state benefits |


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

