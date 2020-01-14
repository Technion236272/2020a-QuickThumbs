const admin = require('firebase-admin');
const functions = require('firebase-functions')
admin.initializeApp(functions.config().firebase);
const functionTriggers = functions.region('europe-west1').firestore;
const db = admin.firestore()

		
		
		
	exports.user_sent_request = functions.firestore.document('/users/{user_id}/requests/{friend_id}').onCreate((snap, context) => {
    const user_id = context.params.user_id;
    const friend_id = context.params.friend_id;


    console.log('friend request notification event triggered');


    db.collection('users').doc(user_id).get().then((doc) => {

        const user_name = doc.data().name;

        // Create a notification
        const payload = {
            notification: {
                title: 'New pending request',
                body: user_name + ' want you to play with him!',
                sound: "default"
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };

        return admin.messaging().sendToTopic("user_sent_request" + friend_id, payload, options);

    }).catch(error => {

        console.log(error.message);
    });


});


exports.friend_accepted_user_request = functions.firestore.document('/users/{friend_id}/friends/{user_id}').onCreate((snap, context) => {
    const user_id = context.params.user_id;
    const friend_id = context.params.friend_id;

    console.log('friend approved your friendship request notification event triggered');

    db.collection('users').doc(user_id).get().then((doc) => {

        const friend_name = doc.data().name;

        // Create a notification
        const payload = {
            notification: {
                title: 'Request accepted',
                body: friend_name + ' is happy to play with you!',
                sound: "default"
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };
        let deleteDoc = db.collection('users').doc(friend_id).collection('requests').doc(user_id).delete();

        return admin.messaging().sendToTopic("friend_accepted_user_request" + friend_id, payload, options);

    }).catch(error => {

        console.log(error.message);
    });
});

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
