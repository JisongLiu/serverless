
app.factory('Customers', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/', {}, {
        create: {
            method: 'POST'
        }
    });
}).factory('Customer', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/:email', {}, {
        get: {
            method: 'GET',
            interceptor: {
                response: function(response) {
                    var result = response.resource;
                    result.$status = response.status;
                    return result;
                }
            }
        },
        update: {
            method: 'PUT'
        }
    });
}).factory('CustomerAddress', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/:email/address', {});
}).factory('Address', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/addresses/:id', {}, {
        create: {
            method: 'POST'
        },
        update: {
            method: 'PUT'
        }
    });
}).factory('Recommendation', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/composite/:email/content', {});
}).factory('Content', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/contents/:id', {});
}).factory('Comment', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/comments/:id', {}, {
        create: {
            method: 'POST'
        }
    });
});

app.service('popupService', function($window) {
    this.showPopup = function(message) {
        return $window.confirm(message);
    }
});
