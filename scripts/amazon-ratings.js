var colseparator = "\t";
var headers = ["customer", "asin", "rating"];
print(headers.join(colseparator));

db.reviews.find().forEach( function(doc) {

    var entry = {
	customer: doc.customer,
	asin: doc.asin,
	rating: doc.rating
    };

    var row = [];
    for(i in headers) {
	row = row.concat(entry[headers[i]]);
    }

    print(row.join(colseparator));

});
