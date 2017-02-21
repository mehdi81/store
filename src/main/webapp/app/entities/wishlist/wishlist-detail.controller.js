(function() {
    'use strict';

    angular
        .module('storeApp')
        .controller('WishlistDetailController', WishlistDetailController);

    WishlistDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Wishlist', 'User'];

    function WishlistDetailController($scope, $rootScope, $stateParams, previousState, entity, Wishlist, User) {
        var vm = this;

        vm.wishlist = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('storeApp:wishlistUpdate', function(event, result) {
            vm.wishlist = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
