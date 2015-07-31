var colseparator = "\t";
var headers = ["asin", "title", "group", "salesrank", "averageRating", "categories"];
print(headers.join(colseparator));

db.products.find().forEach( function(doc) {

    var merged = [];
    for(i in doc.categories) {
        var cl = doc.categories[i];
        for(j in cl) {
            merged = merged.concat(cl[j].name);
        }
    }

    var entry = {
	asin: doc.asin,
	title: doc.title,
	group: doc.group,
	salesrank: doc.salesrank,
	averageRating: doc.overallReview.averageRating, 
        categories: merged.join("::")
    };

    var row = [];
    for(i in headers) {
	row = row.concat(entry[headers[i]]);
    }

    print(row.join(colseparator));

});
