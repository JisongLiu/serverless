var app = angular.module('app', ['ngResource'])
.config(['$httpProvider', function($httpProvider) {
        $httpProvider.defaults.useXDomain = true;
        delete $httpProvider.defaults.headers.common['X-Requested-With'];
    }
]);

app.factory('CustomersFactory', function ($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers', {}, {
        query: {
            method: 'GET',
            params: {},
            isArray: false
        }
    })
});

app.factory('CustomerFactory', function ($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/:email', {}, {
        query: {
            method: 'GET',
            params: {},
            isArray: false
        }
    })
});

app.controller('CustomerListController', ['$scope', 'CustomersFactory', function CustomerListController($scope, CustomersFactory) {
    // CustomerFactory.get({}, function (customerFactory) {
    //     $scope.email = customerFactory.email;
    //     $scope.firstname = customerFactory.firstName;
    //     $scope.lastname = customerFactory.lastname;
    //     $scope.phonenumber = customerFactory.phonenumber;
    //     $scope.address_ref = customerFactory.address_ref;
    // })
    $scope.customers = CustomersFactory.query();
}]);
