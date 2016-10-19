var app = angular.module('app', ['ui.router', 'ngResource']);
app.config(function($stateProvider, $httpProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];

    $stateProvider.state({
        name: 'customerList',
        url:'/customers',
        templateUrl:'partials/customer-list.html',
        controller:'CustomerListController'
    }).state({
        name: 'newCustomer',
        url:'/customers/new',
        templateUrl:'partials/customer-add.html',
        controller:'CustomerCreateController'
    }).state({
        name: 'editCustomer',
        url:'/customers/:email/edit',
        templateUrl:'partials/customer-edit.html',
        controller:'CustomerEditController'
    }).state({
        name: 'addressList',
        url:'/addresses',
        templateUrl: 'partials/address-list.html',
        controller: 'AddressListController'
    });
}).run(function($state) {
    $state.go('customerList');
});

app.factory('Customers', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/', {});
}).factory('Customer', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/:email', {}, {
        update: {
            method: 'PUT'
        }
    })
}).factory('CustomerAddress', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/customers/:email/address', {});
}).factory('Address', function($resource) {
    return $resource('https://k58s2zp6g8.execute-api.us-east-1.amazonaws.com/beta/addresses/:id', {}, {
        update: {
            method: 'PUT'
        }
    })
});

app.service('popupService', function($window) {
    this.showPopup = function(message) {
        return $window.confirm(message);
    }
});

app.controller('CustomerListController', function CustomerListController($scope, $window, $state, popupService, Customers, Customer) {
    $scope.customers = Customers.get();
    $scope.deleteCustomer = function(customer) {
        if (popupService.showPopup('Really delete customer?')) {
            Customer.delete({"email": customer.email}, function() {
                $state.go('customerList');
            });
        }
    };
}).controller('CustomerEditController', function($scope, $state, $stateParams, Customer, Address) {
    $scope.customer = Customer.get({ email: $stateParams.email }, function(c) {
        $scope.address  = Address.get({ id: c.item[0].address_ref });
    });

    $scope.updateCustomer = function() {
        // TODO
        console.log($scope.customer.item[0]);
        console.log($scope.address.item[0]);
    };
}).controller('CustomerCreateController', function($scope) {
    $scope.customer = {};
    $scope.address  = {};
    $scope.addCustomer = function() {
        // TODO
        console.log($scope.customer);
        console.log($scope.address);
    };
}).controller('AddressListController', function($scope) {
    // TODO: optional
});
