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
        create: {
            method: 'POST'
        },
        update: {
            method: 'PUT'
        }
    })
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
}).controller('CustomerEditController', function($scope, $state, $stateParams, popupService, Customer, Address) {
    $scope.customer = Customer.get({ email: $stateParams.email }, function(c) {
        $scope.address  = Address.get({ id: c.items[0].address_ref });
    });

    // smartystreets auto-complete plugin
    $scope.ss = jQuery.LiveAddress({
        // Set key with requesting host's ip
        key: '22506383748684446',
        waitForStreet: true,
        debug: false,
        target: "US",
        addresses: [{
            address1: "#address-1",
            address2: "#address-2",
            locality: "#city",
            administrative_area: "#state",
            postal_code: "#zipcode"
        }]
    });

    $scope.updateCustomer = function() {
        var addressID = $scope.ss.getMappedAddresses()[0].id();
        $scope.ss.verify(addressID, function(response) {
            if (response.isValid()) {
                // if (response.isMissingSecondary()) {
                //     popupService.showPopup("don't forget your apartment number!");
                //     return ;
                // }
                $scope.$apply(function () {

                    // variables indicating whether each field has been modified
                    var fname = $("#firstname").hasClass("ng-dirty");
                    var lname = $("#lastname").hasClass("ng-dirty");
                    var phone = $("#phonenumber").hasClass("ng-dirty");
                    var line1 = $("#address-1").hasClass("ng-dirty");
                    var line2 = $("#address-2").hasClass("ng-dirty");
                    var city = $("#city").hasClass("ng-dirty");
                    var state = $("#state").hasClass("ng-dirty");
                    var zipcode = $("#zipcode").hasClass("ng-dirty");
                    var addressDirty = line1 || line2 || city || state || zipcode;
                    var customerDirty = fname || lname || phone || addressDirty;

                    if (customerDirty) {
                        var customerUpdate = {};
                        var data = $scope.customer.items[0];
                        if (fname) { customerUpdate.firstname = $("#firstname").val(); }
                        if (lname) { customerUpdate.lastname = $("#lastname").val();   }
                        if (phone) { customerUpdate.phonenumber = $("#phonenumber").val(); }
                        if (addressDirty) {
                            var barcode = response.raw[0].delivery_point_barcode;
                            var addressUpdate = {id: barcode};
                            var addrData = $scope.address.items[0];
                            addressUpdate.line1 = $("#address-1").val();
                            addressUpdate.line2 = $("#address-2").val();
                            addressUpdate.city = $("#city").val();
                            addressUpdate.state = $("#state").val();
                            addressUpdate.zipcode = $("#zipcode").val();

                            console.log("updating address");
                            console.log(addressUpdate);
                            Address.update({id: barcode}, addressUpdate);
                            
                            customerUpdate.address_ref = barcode;
                        }
                        
                        console.log("updating customer");
                        console.log(customerUpdate);
                        Customer.update({email: $stateParams.email}, 
                                        customerUpdate, 
                                        function() {
                            $state.go('customerList');
                        });
                        
                    }

                });

            } else {
                popupService.showPopup("please enter a valid address");
            }
        });
        // console.log($scope.customer.item[0]);
        // console.log($scope.address.item[0]);
    };
}).controller('CustomerCreateController', function($scope) {
    $scope.customer = {};
    $scope.address  = {};
    $scope.addCustomer = function() {
        // TODO
        console.log($scope.customer);
        console.log($scope.address);
    };
}).controller('AddressListController', function($scope, Address) {
    // TODO: optional
    $scope.addresses = Address.get();
});
