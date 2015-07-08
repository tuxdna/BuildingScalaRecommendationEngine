package model

import play.api.libs.json.Json

import reactivemongo.bson.Macros

case class Customer(
  Number: Int,
  Gender: String,
  NameSet: String,
  Title: String,
  GivenName: String,
  MiddleInitial: String,
  Surname: String,
  StreetAddress: String,
  City: String,
  // State: String,
  StateFull: String,
  // ZipCode: String,
  Country: String,
  CountryFull: String,
  EmailAddress: String //  
  )

object Customer {
  implicit val reviewHandler = Macros.handler[Customer]
  implicit val reviewFormat = Json.format[Customer]
}


//
// can't have more than 22 members in case class
//
// case class CustomerData(
//   Number: Int,
//   Gender: String,
//   NameSet: String,
//   Title: String,
//   GivenName : String,
//   MiddleInitial : String,
//   Surname : String,
//   StreetAddress : String,
//   City : String,
//   State : String,
//   StateFull : String,
//   ZipCode : String,
//   Country : String,
//   CountryFull : String,
//   EmailAddress : String,
//   Username : String,
//   Password : String,
//   BrowserUserAgent : String,
//   TelephoneNumber : String,
//   TelephoneCountryCode : Int,
//   MothersMaiden : String,
//   Birthday : String,
//   TropicalZodiac : String,
//   CCType : String,
//   CCNumber : BigInt,
//   CVV2 : Int,
//   CCExpires : String,
//   NationalID : String,
//   UPS : String,
//   WesternUnionMTCN : BigInt,
//   MoneyGramMTCN : BigInt,
//   Color : String,
//   Occupation : String,
//   Company : String,
//   Vehicle : String,
//   Domain : String,
//   BloodType : String,
//   Pounds : Double,
//   Kilograms : Double,
//   FeetInches : String,
//   Centimeters : Int,
//   GUID : String,
//   Latitude : Double,
//   Longitude : Double
// )



