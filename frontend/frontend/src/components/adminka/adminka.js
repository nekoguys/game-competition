import React from "react";

import Select from 'react-select';

import "./adminka.css";
import {NavbarHeaderWithFetcher as NavbarHeader} from "../app/app";
import DefaultTextInput from "../common/default-text-input";
import DefaultSubmitButton from "../common/default-submit-button";
import {ADMINKA_PAGE_SIZE} from "../../helpers/constants";
import ApiHelper from "../../helpers/api-helper";
import showNotification from "../../helpers/notification-helper";
import withRedirect from "../../helpers/redirect-helper";
import {isAdmin} from "../../helpers/role-helper";
import withAuthenticated from "../../helpers/with-authenticated";


class Modal extends React.Component {
    constructor(props) {
        super(props)
    }

    render() {
        console.log({props: this.props})
        if (!this.props.show) {
            return null;
        }
        return (
            <div style={{paddingTop: "10px"}}>
            <div className={"mymodal"}>
                <div className={"content"}>
                {this.props.children}
                </div>
                <div className={"actions"} style={{marginBottom: "-10px"}}>
                    <div className={"col d-flex justify-content-center"}>
                        <button onClick={this.props.onOk}>Ok</button>
                    </div>
                    <div className={"col d-flex justify-content-center"}>
                        <button onClick={this.props.onCancel}>Отмена</button>
                    </div>
                </div>
            </div>
            </div>
        )
    }
}


class UsersCollection extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            isShowingModal: false,
            lastChangePasswordEmail: ""
        }
    }

    handleRoleChange = ({email, role}) => {
        console.log({email, role});
        this.props.handleRoleChange({email, role});
    };

    onShowModal = (email) => {
        console.log(email)
        this.setState({isShowingModal: true, lastChangePasswordEmail: email})
    }

    onCancelModal = () => {
        this.setState({isShowingModal: false, lastChangePasswordEmail: ""});
    }

    onOkModal = () => {
        ApiHelper
            .adminkaChangePassword({userEmail: this.state.lastChangePasswordEmail})
            .then(resp => {
                console.log({resp});
                if (resp.status >= 300) {
                    return {success: false, json: resp.text()};
                }

                return {success: true, json: resp.json()};
            }).then(resp => {
                resp.json.then(jsonBody => {
                    console.log(jsonBody);
                    if (resp.success) {
                        showNotification(this).success(jsonBody.message, "Success, password reset to 1234", 2500);
                    } else {
                        showNotification(this).success(jsonBody.message, "Error", 5000);
                    }
                    this.setState({isShowingModal: false, lastChangePasswordEmail: ""});
                })
        })
    }

    render() {
        const items = this.props.items;
        console.log(this.state);
        return (
            <div>
            <div className={"user-grid user-grid-padding"}>
                {items.map(el => {
                    return (
                        <UserGridItem onShowModal={this.onShowModal} item={el} key={el.email} handleRoleChange={this.handleRoleChange}/>
                    )
                })}
            </div>
                <div>
                    <Modal onCancel={this.onCancelModal} onOk={this.onOkModal} show={this.state.isShowingModal}>SomeTextInside</Modal>
                </div>
            </div>
        )
    }
}


class UserGridItem extends React.Component {
    constructor(props) {
        super(props);

        this.options = [
            {value: "ROLE_STUDENT", label: "Студент"},
            {value: "ROLE_TEACHER", label: "Преподаватель"},
            {value: "ROLE_ADMIN", label: "Админ"}
        ];

        this.state = {
            selectedOption: this.options.filter(option => option.value === props.item.role)
        };
    }

    isAdmin = () => {
        return this.props.item.role === "ROLE_ADMIN";
    }

    handleRoleChange = (selectedOption) => {
        this.setState(
            { selectedOption, newRole: selectedOption.value },
            () => {
                console.log(`Option selected:`, this.state.selectedOption);
                this.props.handleRoleChange({email: this.props.item.email, role: selectedOption.value});
            }
        );
    };

    showChangePasswordDialogModal = () => {
        this.props.onShowModal(this.props.item.email);
    }

    render() {
        const {item} = this.props;
        const {selectedOption} = this.state;
        console.log({state: this.state});

        let options = [...this.options];
        if (!this.isAdmin()) {
            options = options.slice(0, 2);
        }

        const buttonStyle = {
            backgroundColor: "#48BDFF",
            borderWidth: "0",
            height: "100%",
            lineHeight: "1.5"
        };

        const selectOptions = {
            value: selectedOption,
            onChange: this.handleRoleChange,
            options: options,
            isDisabled: this.isAdmin(),
            styles: {
                container: (provided, state) => {
                    // none of react-select's styles are passed to <Control />
                    return {...provided, width: "100%", fontSize: "1em", marginLeft: 20};
                },
                valueContainer: (provided, _) => {
                    return {...provided, padding: 0, margin: 0, textAlign: "right"}
                },
                singleValue: (provided, state) => {
                    return { ...provided, right: 0};
                },
            }
        };

        const MySelect = props => (
            <Select
                {...props}
            />
        );

        return (
            <div className={"grid-item"}>
                <div className={"row justify-content-between"}>
                    <div style={{textAlign: "left"}}>
                        email:
                    </div>
                    <div style={{textAlign: "right"}}>
                        {item.email}
                    </div>
                </div>
                <div className={"row justify-content-between"} style={{paddingTop: "10px"}}>
                    <div style={{textAlign: "left"}} className={"grid-item-line"}>
                        Роль:
                    </div>
                    <div className={"col"} style={{paddingLeft: 0}}>
                    <MySelect {...selectOptions}/>
                    </div>
                </div>
                <div style={{paddingTop: "20px", marginBottom: "-10px"}}>
                    <DefaultSubmitButton onClick={this.showChangePasswordDialogModal} style={buttonStyle} text={"Сбросить пароль"} isDisabled={this.isAdmin()}/>
                </div>
            </div>
        )
    }
}

class AdminkaComponent extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            items: [],
            isShowingChanges: false
        };
        this.searchString = "";
        this.roleChanges = {};
        this.lastFetchedPage = -1;
    }

    handleRoleChange = ({email, role}) => {
        this.roleChanges[email] = role;
        console.log({roleChanges: this.roleChanges});

        if (this.state.items.filter(el => el.email === email)[0].role === role) {
            delete this.roleChanges[email];
        }
    };

    // call after fetching user roles from server, for showing already modified roles
    processServerUsers = (userList) => {
        return userList.map(el => {
            let {email} = el;
            if (email in this.roleChanges) {
                return {email, role: this.roleChanges[email]};
            }
            return el;
        })
    };

    onDoSearch = (shouldResetLastFetchedPage) => {
        console.log(`Request with search string: ${this.searchString}`);
        //request
        if (shouldResetLastFetchedPage) {
            this.lastFetchedPage = -1;
        }
        const query = {
            query: this.searchString,
            page: this.lastFetchedPage + 1,
            pageSize: ADMINKA_PAGE_SIZE
        };

        ApiHelper.adminkaSearchUsers(query).then(resp => {
            if (resp.status > 300) {
                return {success: false, json: resp.text()}
            }
            return {success: true, json: resp.json()}
        }).then(el => {
            if (!el.success) {
                el.json.then(text => {
                    showNotification(this).error("Ошибка", text, 3000);
                })
            } else {
                el.json.then(jsonBody => {
                    this.lastFetchedPage += 1;
                    this.setState(prevState => {
                        console.log(this.lastFetchedPage);
                        if (this.lastFetchedPage === 0) {
                            return {
                                items: this.processServerUsers(jsonBody.results),
                                isShowingChanges: false
                            }
                        } else {
                            return {
                                items: this.processServerUsers(prevState.items.concat(jsonBody.results))
                            }
                        }
                    })
                })
            }
        })
    };

    onSaveButtonClick = () => {
        Promise.all(Object.keys(this.roleChanges).map(el => {
            return ApiHelper.changeRole(el, this.roleChanges[el])
        })).catch(err => {
            console.log({err});
            showNotification(this).error("Error", err.toString(), 3000);
        }).then(_ => {
            showNotification(this).success("Успех", "Роли успешно изменены", 3000);
        });
    };

    onShowOrHideChanges = () => {
        this.setState(prevState => {
            return {isShowingChanges: !prevState.isShowingChanges};
        })
    };

    getChangesAsArray = () => {
        let arr = [];
        console.log(Object.entries(this.roleChanges));
        for (const [key, value] of Object.entries(this.roleChanges)) {
            arr.push({email: key, role: value});
        }
        console.log({arr});
        return arr;
    };

    render() {
        const inputStyle = {
            width: "100%",
            margin: "0",
            borderRadius: "15px",
            textAlign: "center"
        };

        const buttonStyle = {
            backgroundColor: "#48BDFF",
            borderWidth: "0",
            height: "100%",
            lineHeight: "1.5"
        };

        const items = this.state.isShowingChanges ? this.getChangesAsArray() : this.state.items;

        let loadMoreButton;
        if (!this.state.isShowingChanges && this.state.items.length > 0) {
            loadMoreButton = (
                <div style={{paddingTop: "30px"}}>
                <div style={{margin: "0 auto", width: "30%"}}>
                    <DefaultSubmitButton text={"Показать ещё"} style={buttonStyle} onClick={() => {
                        this.onDoSearch(false);
                    }}/>
                </div>
            </div>)
        }
        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "100px"}}>
                    <div>
                        <div>
                        <div style={{textAlign: "center", fontSize: "1.25rem"}}>Найти пользователя</div>
                        </div>
                        <div className={"row justify-content-end"} style={{paddingTop: "10px"}}>
                            <div className={"col-3"}>
                                <DefaultSubmitButton text={this.state.isShowingChanges ? "Скрыть изменения" : "Показать изменения"}
                                                     style={buttonStyle} onClick={this.onShowOrHideChanges}/>
                            </div>
                            <div className={"col-6"}>
                                <DefaultTextInput
                                    placeholder={"Имя, фамилия или email"}
                                    style={inputStyle}
                                    onChange={(text) => this.searchString = text}
                                    onKeyDown={(event) => {
                                        if (event.key === 'Enter') {
                                            event.preventDefault();
                                            event.stopPropagation();
                                            this.onDoSearch(true);
                                        }
                                }}
                                />
                            </div>
                            <div className={"col-3"} style={{marginBottom: "-10px"}}>
                                <DefaultSubmitButton text={"Сохранить изменения"} style={buttonStyle} onClick={this.onSaveButtonClick}/>
                            </div>

                        </div>
                    </div>
                    <div style={{paddingTop: "30px"}}>
                        <UsersCollection showNotification={this.props.showNotification} handleRoleChange={this.handleRoleChange} items={items}/>
                    </div>
                    {loadMoreButton}

                </div>
            </div>
        )
    }
}

export default withAuthenticated(withRedirect(AdminkaComponent, isAdmin));
