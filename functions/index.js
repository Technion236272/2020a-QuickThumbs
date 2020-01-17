const admin = require('firebase-admin');
const functions = require('firebase-functions')
admin.initializeApp(functions.config().firebase);
const functionTriggers = functions.region('europe-west1').firestore;
const db = admin.firestore()


exports.user_sent_request = functions.firestore.document('/users/{user_id}/requests/{friend_id}').onCreate((snap, context) => {
    const user_id = context.params.user_id;
    const friend_id = context.params.friend_id;


    console.log('friend request notification event triggered');


    db.collection('users').doc(friend_id).get().then((doc) => {

        const friend_name = doc.data().name;

        // Create a notification
        const payload = {
            data: {
                title: 'New Friend Request',
                body: friend_name + ' sent you a friend request!',
                sender: friend_id
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };

        return admin.messaging().sendToTopic("user_sent_request" + user_id, payload, options);

    }).catch(error => {

        console.log(error.message);
    });


});


exports.friend_accepted_user_request = functions.firestore.document('/users/{friend_id}/friends/{user_id}').onCreate((snap, context) => {
    const user_id = context.params.user_id;
    const friend_id = context.params.friend_id;

    console.log('friend approved your friendship request notification event triggered');

    const addStatistics = db.collection('users').doc(user_id).collection('stats').doc('statistics').get().then((Doc) => {

                                  const score = Doc.data().TotalScore;
                                  const accuracy = Doc.data().avgAccuracy;
                                  const CPM = Doc.data().avgCPM;
                                  const WPM = Doc.data().avgWPM;
                                  const friend_name = Doc.data().name;

                                  return db.collection('users').doc(friend_id).collection('friends').doc(user_id).set({TotalScore: score, avgAccuracy : accuracy,
                                  			avgCPM : CPM, avgWPM : WPM}, { merge: true });
                              }).catch(error => {
                                  console.log(error.message);
                              });

    console.log('reached after statistics');
    db.collection('users').doc(user_id).get().then((doc) => {

        const user_name = doc.data().name;

        // Create a notification
        const payload = {
            data: {
                title: 'You are Friends!',
                body: user_name + ' is happy to play with you!',
                sender: user_id
            },
        };

        //Create an options object that contains the time to live for the notification and the priority
        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };

        let addName = db.collection('users').doc(friend_id).collection('friends').doc(user_id).set({name: user_name}, { merge: true });
        console.log('reached after addName');
        let deleteDoc = db.collection('users').doc(friend_id).collection('requests').doc(user_id).delete();
        console.log('reached after deleteDoc');
        let addToOtherFriend = db.collection('users').doc(user_id).collection('friends').doc(friend_id).set({uid: friend_id}, { merge: true });
        console.log('reached after addToOtherFriend');
        return admin.messaging().sendToTopic("friend_accepted_user_request" + friend_id, payload, options);
    }).catch(error => {
        console.log(error.message);
    });
});

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
