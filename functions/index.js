const admin = require('firebase-admin');
const functions = require('firebase-functions')
admin.initializeApp(functions.config().firebase);
const functionTriggers = functions.region('europe-west1').firestore;
const db = admin.firestore()

		
		
		

exports.user_game_invite = functions.firestore.document('/users/{user_id}/game_requests/{friend_id}').onCreate((snap, context) => {
    const user_id = context.params.user_id;
    const friend_id = context.params.friend_id;
    console.log('user_id ' + user_id);
    console.log('friend_id ' + friend_id);


    console.log('game invite request notification event triggered');

    db.collection('users').doc(user_id).collection('game_requests').doc(friend_id).get().then((doc) => {
		console.log(doc);
		const room_key	= doc.data().roomKey;
		console.log(room_key);

        // Create a notification
        const payload = {
            data: {
                title: 'Game invite',
				room: room_key,
				sender: user_id,
                body: user_id + ' invite you for a game against him!',
                sound: "default"
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 10
        };
		
		let deleteDoc = db.collection('users').doc(user_id).collection('game_requests').doc(friend_id).delete();

        return admin.messaging().sendToTopic("user_game_invite" + friend_id, payload, options);

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
