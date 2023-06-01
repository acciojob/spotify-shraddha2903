package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        for(User curUser: users){
            if(curUser.getMobile().equals(mobile)){
                return curUser;
            }
        }
        User user_obj = new User(name,mobile);
        users.add(user_obj);
        return user_obj;
    }

    public Artist createArtist(String name) {
        for(Artist artist: artists){
            if(artist.getName().equals(name))
                return artist;
        }
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }
    public boolean isArtistExist(String artistName)
    {
        for(Artist artist : artists)
        {
            if(artist.getName().equals(artistName))
            {
                return  true;
            }
        }
        return false;
    }
    public Artist getArtists(String artistName)
    {
        Artist artistObj = new Artist();
        for(Artist artist : artists)
        {
            if(artist.getName().equals(artistName))
            {
                artistObj = artist;
            }
        }
        return artistObj;

    }

    public Album createAlbum(String title, String artistName) {
        boolean isArtistExist;
        for(Album album : albums){
            if(album.getTitle().equals(title))
                return  album;
        }
        Artist artist;
        if(!isArtistExist(artistName))
        {
            artist = new Artist(artistName);
        }
        else {
            artist=getArtists(artistName);
        }
       //putting album
        Album album = new Album(title);
        albums.add(album);

        //putting in artist-album map
        List albumList = artistAlbumMap.getOrDefault(artist,new ArrayList<>());
        albumList.add(album);
        artistAlbumMap.put(artist,albumList);

        return album;
    }


    public Song createSong(String title, String albumName, int length) throws Exception{
        if(isAlbumExist(albumName))
        {
            Song song = new Song(title,length);
            songs.add(song);
            //get album
            Album album = getAlbumByName(albumName);
            //get list of song corresponds to album
            List<Song> songList = albumSongMap.getOrDefault(album,new ArrayList<>());
            songList.add(song);
            albumSongMap.put(album,songList);
            return song;
        }
        throw new Exception("Album does not exist");
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {

        if(!isUserExist(mobile))
        {
            throw new Exception("User does not exist");
        }
        List<Song> songListOflength = getSongListOfGivenLength(length);
        Playlist playlist = new Playlist(title);
        playlistSongMap.put(playlist,songListOflength);
        User user = getUserByMobileNumer(mobile);
        //add in playlist listener map
        addPlayListListener(playlist,user);
        //add in playList creator map
        addCreatorPlayListMap(user,playlist);
        //add in user play list map
        addUserPlayList(user,playlist);


        return playlist;

    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {

        if(!isUserExist(mobile))
        {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        List<Song> songList = getSongListOfGivenTitle(songTitles);
        playlistSongMap.put(playlist,songList);

        User user = getUserByMobileNumer(mobile);
        //add in playlist listener map
        addPlayListListener(playlist,user);
        //add in playList creator map
        addCreatorPlayListMap(user,playlist);
        //add in user play list map
        addUserPlayList(user,playlist);

        return playlist;

    }



    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        if (!isUserExist(mobile))
            throw new Exception("User does not exist");
        if(!isPlayListExist(playlistTitle))
        {
            throw new Exception("Playlist does not exist");
        }
        Playlist playlist = getPlayListByTitle(playlistTitle);
        User user = getUserByMobileNumer(mobile);
        if(!isUserCreator(user) && !isUserListener(user))
        {
            addPlayListListener(playlist,user);
            addUserPlayList(user,playlist);
        }
        return playlist;

    }



    public Song likeSong(String mobile, String songTitle) throws Exception {
        if(!isUserExist(mobile))
            throw new Exception("User does not exist");
        if(!isSongExist(songTitle))
            throw new Exception("Song does not exist");
        Song song = getSongByTitle(songTitle);
        User user = getUserByMobileNumer(mobile);
        List<User> userList=songLikeMap.getOrDefault(song,new ArrayList<>());
        if(userList.isEmpty())
        {
            userList.add(user);
            int lk=song.getLikes();
            song.setLikes(lk+1);
        }
        else {
            boolean liked=false;
            for (User us : userList) {
                if (us.getMobile() == mobile) {
                    liked=true;
                }
                if(!liked){
                    userList.add(user);
                    int lk=song.getLikes();
                    song.setLikes(lk+1);
                }
            }
        }
        songLikeMap.put(song,userList);
        ///updating artist likes
        Album album = getalbumBySongTitle(songTitle);
        Artist artist = getArtistByAlbumName(album.getTitle());
        int likes=artist.getLikes();
        artist.setLikes(likes+1);

        return song;

    }


    public String mostPopularArtist() {
        int max = Integer.MIN_VALUE;
        String name="";
        for(Artist artist : artists)
        {
            if(artist.getLikes()>max)
            {
                max= artist.getLikes();
                name = artist.getName();

            }
        }
        return name;
    }

    public String mostPopularSong() {
        int max = Integer.MIN_VALUE;
        String name="";
        for(Song song : songs)
        {
            if(song.getLikes()>max)
            {
                max= song.getLikes();
                name = song.getTitle();

            }
        }
        return name;
    }

    public boolean isAlbumExist(String albumName) {
        for (Album album : albums)
        {
            if(album.getTitle().equals(albumName))
            {
                return true;
            }
        }
        return false;
    }
    private Album getAlbumByName(String albumName)
    {
        Album albumObj = null;
        for(Album album : albums)
        {
            if(album.getTitle().equals(albumName))
            {
                albumObj = album;
            }
        }
        return albumObj;
    }
    //check user by mobile number
    private boolean isUserExist(String mobile)
    {
        boolean isExist=false;
        for(User user : users)
        {
            if(user.getMobile().equals(mobile))
                isExist=true;
        }
        return isExist;
    }
    private List<Song> getSongListOfGivenLength(int length) {
        List<Song> ListOfSong = new ArrayList<>();
        for(Song song : songs)
        {
            if(song.getLength()==length)
            {
                ListOfSong.add(song);
            }
        }
        return ListOfSong;
    }

    private User getUserByMobileNumer(String mobile) {
        User user = null;
        for(User userI : users)
        {
            if(userI.getMobile().equals(mobile))
            {
                user=userI;
            }
        }
        return user;
    }
    private void addPlayListListener(Playlist playlist, User user) {
        List<User>userList = new ArrayList<>();
        if(playlistListenerMap.containsKey(playlist))
        {
            userList=playlistListenerMap.get(playlist);
        }
        userList.add(user);
        playlistListenerMap.put(playlist,userList);
    }

    private void addUserPlayList(User user, Playlist playlist) {
        List<Playlist> playlistsList=new ArrayList<>();
        if(userPlaylistMap.containsKey(user))
        {
            playlistsList = userPlaylistMap.get(user);
        }
        playlistsList.add(playlist);
        userPlaylistMap.put(user,playlistsList);

    }

    private void addCreatorPlayListMap(User user, Playlist playlist) {
        if(!creatorPlaylistMap.containsKey(user))
        {
            creatorPlaylistMap.put(user,playlist);
        }
    }

    private List<Song> getSongListOfGivenTitle(List<String> songTitles) {
        List<Song> songs = new ArrayList<>();
        for(String songI:songTitles)
        {
            Song songByTitle = getSongByTitle(songI);
            songs.add(songByTitle);
        }
        return songs;
    }

    private Song getSongByTitle(String song) {
        Song res=new Song();
        for(Song sg : songs)
        {
            if(sg.getTitle().equals(song))
            {
                res = sg;
            }
        }
        return res;
    }

    private boolean isUserListener(User user) {
        if(userPlaylistMap.containsKey(user))
            return true;
        return false;
    }

    private boolean isUserCreator(User user) {
        if(creatorPlaylistMap.containsKey(user))
            return true;
        return false;
    }

    private Playlist getPlayListByTitle(String playlist) {
        Playlist playlist1 = null;
        for(Playlist pl : playlists)
        {
            if(pl.getTitle().equals(playlist))
            {
                playlist1 = pl;
            }
        }
        return playlist1;
    }

    private boolean isPlayListExist(String playlistTitle) {
        boolean isExist=false;
        for(Playlist playlist : playlists)
        {
            if(playlist.getTitle().equals(playlistTitle))
                isExist=true;
        }
        return isExist;

    }

    private Artist getArtistByAlbumName(String album) {
        Artist artist;
        for (Map.Entry<Artist,List<Album>> hm : artistAlbumMap.entrySet())
        {
            List<Album> albums1 = hm.getValue();
            for(Album am : albums1)
            {
                if(am.getTitle().equals(album))
                {
                    artist=hm.getKey();
                    return artist;
                }
            }
        }
        return null;
    }

    private Album getalbumBySongTitle(String songTitle) {
        Album album;
        for(Map.Entry<Album,List<Song>> hm : albumSongMap.entrySet())
        {
            List<Song> songList = hm.getValue();
            for(Song sg : songList)
            {
                if(sg.getTitle().equals(songTitle))
                {
                    album =hm.getKey();
                    return album;
                }
            }
        }
        return null;
    }

    private boolean isSongExist(String songTitle) {
        boolean isExist=false;
        for (Song song : songs)
        {
            if(song.getTitle().equals(songTitle))
                isExist=true;
        }
        return isExist;
    }


}
