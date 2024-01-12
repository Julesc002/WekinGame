import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_URL } from '../config';
import BackgroundWiki from "./BackgroundWiki";
import MessageErreur from "./MessageErreur";

function AdminWiki() {
    const { wikiId} = useParams();
    const [wiki, setWiki] = useState(null);
    const [admin, setadmin] = useState([]);
    const [pseudo, setPseudo] = useState("");
    const [admindata, setadmindata] = useState([]);
    const [errorMessage, setErrorMessage] = useState("");
    const [deleteErrorMessage, setDeleteErrorMessage] = useState("");
    const navigate = useNavigate();

    const handleRetourClick = () => {
        navigate(-1);
    };

    useEffect(() => {
        getEntree();
    },[wikiId]);

    useEffect(()=>{
        if (admin && admin[0]) {
            setadmindata(admin[0].adminsdata);
            console.log(admin.adminsdata);
        }
    },[admin]);


    const getEntree = () => {
        axios.get(`${API_URL}/wiki/${wikiId}/content/-1`).then((res) => {
            setWiki(res.data);
        }).catch((error) => {
            console.error(error);
        });
        axios.get(`${API_URL}/wiki/${wikiId}/admin`).then((res) => {
            setadmin(res.data);
            console.log(res.data);
        });
    };

    const addToAdmin = (pseudo) => {
        if (pseudo.trim().length === 0) {
            setErrorMessage("Veuillez insérer un pseudo");
        } else {
            setErrorMessage("");
            const message ={
                pseudo: pseudo
            }
            axios.put(API_URL + `/wiki/${wikiId}/admin/add`,message)
            .then((response) => {
                if(response.status === 200){
                    const errorElement = document.getElementById("adminadderror");
                    errorElement.innerHTML ="";
                    navigate(0);
                }
            })
            .catch((error)=>{
                if (error.response.status) {
                    const errorElement = document.getElementById("adminadderror");
                    errorElement.innerHTML ="Erreur lors de l'ajout de l'utilisateur au fichier administateurs";
                }
            });
        }
    };

    const isUserOwner = () => {
        if (localStorage.getItem('account') !== null && wiki) {
            return parseInt(localStorage.getItem('account')) === wiki.owner;
        }
        return false;
    }

    const handleSupprAdmin = (index,id) => {
        const message = {
            pseudo: id
        }
        axios.put(API_URL + `/wiki/${wikiId}/admin/delete`,message)
        .then((response) => {
            if(response.status === 200){
                const errorElement = document.getElementById("admindeleteerror");
                errorElement.innerHTML ="";
                navigate(0);
            }
        }).catch((error)=>{
            if (error.response.status){   
                const errorElement = document.getElementById("admindeleteerror");
                errorElement.innerHTML ="Erreur lors de la suppression";
            }
        });
    };

    const majPseudo = (e) => {
        setPseudo(e.target.value);
    };
    
    if (isUserOwner()){
        return (
            <div className="flex-down">
                <BackgroundWiki id={wikiId} />
                {admin && admin[0] && (
                    <div>
                        <h2>Gestion des administrateurs :</h2>
                        <label class="append flex-down">
                            Ajouter un adinistrateur :
                    <div>
                            <input type="text" placeholder='pseudo' onChange={(e) => majPseudo(e)}/>
                                <button onClick={()=>addToAdmin(pseudo)}> Nouvel Administateur </button>
                            </div>
                        </label>
                        <p id="adminadderror">{errorMessage}</p>
                        <div id="ici">
                            <h3>Administateurs Actuels :</h3>
                            {admin && admin.map(function (donnee, index) {
                                return (
                                    <div key={index} class="small-box-content flex-down">
                                            <div class="flex-spaced">
                                                <p>{donnee.adminsdata.pseudo}</p>
                                                {donnee.adminsdata._id != localStorage.getItem('account') ? (
                                                    <button class="float-right" onClick={() => handleSupprAdmin(index,donnee.adminsdata.pseudo)}>Supprimer les droits</button>
                                                ) : null}
                                            </div>
                                    </div>
                                );
                            })}
                            <p id="admindeleteerror"></p>
                        </div>
                    <br/>
                    <button onClick={handleRetourClick}>Retour</button>
                </div>
                )}
            </div>
        );
    }
    else{
        return(
            <MessageErreur/>
        )
    }
}

export default AdminWiki;